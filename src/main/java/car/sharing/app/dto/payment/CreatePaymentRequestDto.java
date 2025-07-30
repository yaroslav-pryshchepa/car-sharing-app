package car.sharing.app.dto.payment;

import car.sharing.app.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreatePaymentRequestDto {

    @NotNull
    private Long rentalId;

    @NotNull
    private PaymentType paymentType;
}
