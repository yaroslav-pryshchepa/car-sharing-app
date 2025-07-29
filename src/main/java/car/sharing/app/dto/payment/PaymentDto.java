package car.sharing.app.dto.payment;

import car.sharing.app.model.PaymentStatus;
import car.sharing.app.model.PaymentType;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PaymentDto {

    private Long id;
    private PaymentStatus status;
    private PaymentType type;
    private Long rentalId;
    private BigDecimal amountToPay;
    private String sessionUrl;
    private String sessionId;
}

