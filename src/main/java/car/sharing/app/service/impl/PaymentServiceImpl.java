package car.sharing.app.service.impl;

import car.sharing.app.dto.payment.CreatePaymentRequestDto;
import car.sharing.app.dto.payment.PaymentDto;
import car.sharing.app.exception.EntityNotFoundException;
import car.sharing.app.mapper.PaymentMapper;
import car.sharing.app.model.Payment;
import car.sharing.app.model.PaymentStatus;
import car.sharing.app.model.Rental;
import car.sharing.app.model.RoleName;
import car.sharing.app.model.User;
import car.sharing.app.repository.payment.PaymentRepository;
import car.sharing.app.repository.rental.RentalRepository;
import car.sharing.app.service.PaymentService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalRepository rentalRepository;
    private final PaymentMapper paymentMapper;

    @Override
    public PaymentDto createPayment(CreatePaymentRequestDto requestDto,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Rental rental = rentalRepository.findById(requestDto.getRentalId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental not found with ID: " + requestDto.getRentalId()));

        boolean isManager = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.MANAGER);

        if (!isManager && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to create a payment for another user's rental.");
        }

        Payment payment = paymentMapper.toEntity(requestDto);
        payment.setRental(rental);
        payment.setStatus(PaymentStatus.PENDING);

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
}
