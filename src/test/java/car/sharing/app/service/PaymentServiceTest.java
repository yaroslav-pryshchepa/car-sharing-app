package car.sharing.app.service;

import static car.sharing.app.util.PaymentUtil.createPaymentDto1;
import static car.sharing.app.util.PaymentUtil.createPaymentRequestDto;
import static car.sharing.app.util.RentalUtil.createRental;
import static car.sharing.app.util.UserUtil.createUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import car.sharing.app.model.User;
import car.sharing.app.repository.payment.PaymentRepository;
import car.sharing.app.repository.rental.RentalRepository;
import car.sharing.app.service.impl.PaymentServiceImpl;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private StripeService stripeService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private User user;
    private Rental rental;
    private UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        paymentService = new PaymentServiceImpl(paymentRepository, rentalRepository, paymentMapper,
                stripeService, notificationService);
        ReflectionTestUtils.setField(paymentService, "fineMultiplier", 1.5);

        user = createUser(1L);
        rental = createRental(2L);
        uriBuilder = UriComponentsBuilder.fromUriString("http://localhost");
    }

    @Test
    void createPayment_SuccessForCustomer() {
        CreatePaymentRequestDto requestDto = createPaymentRequestDto();
        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.findById(2L)).thenReturn(Optional.of(rental));
        when(paymentRepository.existsByRentalIdAndPaymentTypeAndStatus(
                2L, PaymentType.PAYMENT, PaymentStatus.PAID)).thenReturn(false);

        PaymentResponseDto stripeResponse = new PaymentResponseDto("sess_1", "url");
        when(stripeService.createStripeSession(eq(rental), eq(PaymentType.PAYMENT),
                any(), eq(uriBuilder))).thenReturn(stripeResponse);

        Payment savedPayment = new Payment().setId(10L);
        when(paymentRepository.save(any())).thenReturn(savedPayment);

        PaymentDto expectedDto = createPaymentDto1(10L);
        when(paymentMapper.toDto(any())).thenReturn(expectedDto);

        PaymentDto result = paymentService.createPayment(requestDto, authentication, uriBuilder);

        assertThat(result).isEqualTo(expectedDto);
        verify(paymentRepository).save(any());
    }

    @Test
    void createPayment_ThrowsIfAlreadyPaid() {
        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.findById(2L)).thenReturn(Optional.of(rental));
        when(paymentRepository.existsByRentalIdAndPaymentTypeAndStatus(
                2L, PaymentType.PAYMENT, PaymentStatus.PAID)).thenReturn(true);

        assertThatThrownBy(() ->
                paymentService.createPayment(createPaymentRequestDto(),
                        authentication, uriBuilder))
                .isInstanceOf(PaymentAlreadyExistsException.class);
    }

    @Test
    void createPayment_ThrowsIfAccessDenied() {
        User anotherUser = new User();
        anotherUser.setId(99L);
        rental.setUser(anotherUser);

        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.findById(2L)).thenReturn(Optional.of(rental));

        assertThatThrownBy(() ->
                paymentService.createPayment(createPaymentRequestDto(),
                        authentication, uriBuilder))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getPayments_ReturnsPaymentsForCustomer() {
        when(authentication.getPrincipal()).thenReturn(user);
        when(rentalRepository.findAllByUserId(1L)).thenReturn(List.of(rental));
        when(paymentRepository.findAllByRentalIdIn(List.of(2L))).thenReturn(List.of(new Payment()));
        when(paymentMapper.toDto(any())).thenReturn(createPaymentDto1(1L));

        List<PaymentDto> result = paymentService.getPayments(null, authentication);

        assertThat(result).hasSize(1);
    }

    @Test
    void markPaymentCompleted_UpdatesStatusAndSendsNotification() {
        Payment payment = new Payment().setStatus(PaymentStatus.PENDING)
                .setAmountToPay(BigDecimal.TEN)
                .setRental(rental).setSessionId("sess_123").setPaymentType(PaymentType.PAYMENT);

        when(paymentRepository.findBySessionId("sess_123")).thenReturn(Optional.of(payment));
        Session session = new Session();
        session.setStatus("complete");
        when(stripeService.retrieveSession("sess_123")).thenReturn(session);

        paymentService.markPaymentCompleted("sess_123");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(notificationService).sendMessage(anyString());
    }

    @Test
    void markPaymentCompleted_NoUpdateIfAlreadyPaid() {
        Payment payment = new Payment().setStatus(PaymentStatus.PAID).setRental(rental)
                .setSessionId("sess_1");
        when(paymentRepository.findBySessionId("sess_1")).thenReturn(Optional.of(payment));

        Session session = new Session();
        session.setStatus("complete");
        when(stripeService.retrieveSession("sess_1")).thenReturn(session);

        paymentService.markPaymentCompleted("sess_1");

        verify(paymentRepository, never()).save(any());
        verify(notificationService, never()).sendMessage(any());
    }

    @Test
    void markPaymentCompleted_ThrowsIfPaymentNotFound() {
        when(paymentRepository.findBySessionId("sess_x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.markPaymentCompleted("sess_x"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}

