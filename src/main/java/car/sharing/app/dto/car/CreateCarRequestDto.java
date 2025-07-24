package car.sharing.app.dto.car;

import car.sharing.app.model.TypeName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateCarRequestDto {

    @NotBlank
    private String model;
    @NotBlank
    private String brand;
    @NotNull
    private TypeName typeName;
    @Positive
    private int inventory;
    @Positive
    private BigDecimal dailyFee;
}
