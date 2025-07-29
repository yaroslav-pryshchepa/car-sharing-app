package car.sharing.app.dto.payment;

import car.sharing.app.model.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreatePaymentRequestDto {

    @NotNull
    private Long rentalId;

    @NotNull
    private PaymentType paymentType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    private BigDecimal amountToPay;
}
