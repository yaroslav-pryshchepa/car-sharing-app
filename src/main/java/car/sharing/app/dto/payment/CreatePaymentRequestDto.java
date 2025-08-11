package car.sharing.app.dto.payment;

import car.sharing.app.model.PaymentType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreatePaymentRequestDto {

    @NotNull
    @Positive
    private Long rentalId;

    @NotNull
    private PaymentType paymentType;
}
