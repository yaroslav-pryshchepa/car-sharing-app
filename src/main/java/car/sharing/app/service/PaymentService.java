package car.sharing.app.service;

import car.sharing.app.dto.payment.CreatePaymentRequestDto;
import car.sharing.app.dto.payment.PaymentDto;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface PaymentService {

    PaymentDto createPayment(CreatePaymentRequestDto requestDto, Authentication authentication);

    List<PaymentDto> getPayments(Long userId, Authentication authentication);
}
