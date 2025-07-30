package car.sharing.app.service.impl;

import car.sharing.app.dto.payment.CreatePaymentRequestDto;
import car.sharing.app.dto.payment.PaymentDto;
import car.sharing.app.dto.payment.PaymentResponseDto;
import car.sharing.app.exception.EntityNotFoundException;
import car.sharing.app.exception.PaymentAlreadyExistsException;
import car.sharing.app.mapper.PaymentMapper;
import car.sharing.app.model.Payment;
import car.sharing.app.model.PaymentStatus;
import car.sharing.app.model.PaymentType;
import car.sharing.app.model.Rental;
import car.sharing.app.model.RoleName;
import car.sharing.app.model.User;
import car.sharing.app.repository.payment.PaymentRepository;
import car.sharing.app.repository.rental.RentalRepository;
import car.sharing.app.service.NotificationService;
import car.sharing.app.service.PaymentService;
import car.sharing.app.service.StripeService;
import com.stripe.model.checkout.Session;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;
    private final StripeService stripeService;
    private final NotificationService notificationService;
    @Value("${fine.multiplier}")
    private double fineMultiplier;

    @Override
    @Transactional
    public PaymentDto createPayment(CreatePaymentRequestDto requestDto,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        User currentUser = (User) authentication.getPrincipal();

        Rental rental = rentalRepository.findById(requestDto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException("Rental not found with ID: "
                        + requestDto.getRentalId()));

        boolean isManager = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.MANAGER);

        if (!isManager && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to create a payment for another user's rental.");
        }

        if (requestDto.getPaymentType() == PaymentType.PAYMENT) {
            boolean alreadyPaid = paymentRepository.existsByRentalIdAndPaymentTypeAndStatus(
                    rental.getId(),
                    PaymentType.PAYMENT,
                    PaymentStatus.PAID
            );

            if (alreadyPaid) {
                throw new PaymentAlreadyExistsException("This rental has already been paid.");
            }
        }

        BigDecimal amountToPay = calculateAmountToPay(rental, requestDto.getPaymentType());

        PaymentResponseDto stripeSession = stripeService.createStripeSession(
                rental,
                requestDto.getPaymentType(),
                amountToPay,
                uriBuilder
        );

        Payment payment = new Payment()
                .setRental(rental)
                .setStatus(PaymentStatus.PENDING)
                .setPaymentType(requestDto.getPaymentType())
                .setAmountToPay(amountToPay)
                .setSessionId(stripeSession.sessionId())
                .setSessionUrl(stripeSession.sessionUrl());

        paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Override
    public List<PaymentDto> getPayments(Long userId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        boolean isManager = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.MANAGER);

        if (!isManager && userId != null && !userId.equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to view payments of another user.");
        }

        Long actualUserId = isManager ? userId : currentUser.getId();

        List<Payment> payments;

        if (actualUserId != null) {
            List<Long> rentalIds = rentalRepository.findAllByUserId(actualUserId)
                    .stream()
                    .map(Rental::getId)
                    .toList();

            if (rentalIds.isEmpty()) {
                return Collections.emptyList();
            }

            payments = paymentRepository.findAllByRentalIdIn(rentalIds);
        } else {
            payments = paymentRepository.findAll();
        }

        return payments.stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void markPaymentCompleted(String sessionId) {
        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found by session ID"));
        Session session = stripeService.retrieveSession(sessionId);

        if ("complete".equals(session.getStatus()) && payment.getStatus() != PaymentStatus.PAID) {
            payment.setStatus(PaymentStatus.PAID);
            paymentRepository.save(payment);

            String message = String.format("""
                ✅ Новий платіж виконано!

                💳 Amount: %s$
                👤 User ID: %s
                📦 Rental ID: %s
                🧾 Payment Type: %s
                🔁 Session ID: %s
                    """,
                    payment.getAmountToPay(),
                    payment.getRental().getUser().getId(),
                    payment.getRental().getId(),
                    payment.getPaymentType(),
                    payment.getSessionId()
            );

            notificationService.sendMessage(message);
        }
    }

    private BigDecimal calculateAmountToPay(Rental rental, PaymentType type) {
        BigDecimal dailyFee = rental.getCar().getDailyFee();

        if (type == PaymentType.FINE) {
            LocalDate dueDate = rental.getReturnDate();
            LocalDate today = LocalDate.now();
            long overdueDays = ChronoUnit.DAYS.between(dueDate, today);
            if (overdueDays <= 0) {
                overdueDays = 1;
            }

            return dailyFee.multiply(BigDecimal.valueOf(overdueDays))
                    .multiply(BigDecimal.valueOf(fineMultiplier));
        } else {
            long rentalDays =
                    ChronoUnit.DAYS.between(rental.getRentalDate(), rental.getReturnDate()) + 1;
            return dailyFee.multiply(BigDecimal.valueOf(rentalDays));
        }
    }
}
