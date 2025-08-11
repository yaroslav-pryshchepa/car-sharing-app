package car.sharing.app.service;

import car.sharing.app.dto.payment.PaymentResponseDto;
import car.sharing.app.model.PaymentType;
import car.sharing.app.model.Rental;
import com.stripe.model.checkout.Session;
import java.math.BigDecimal;
import org.springframework.web.util.UriComponentsBuilder;

public interface StripeService {

    PaymentResponseDto createStripeSession(Rental rental, PaymentType type, BigDecimal amount,
            UriComponentsBuilder uriBuilder);

    Session retrieveSession(String sessionId);
}
