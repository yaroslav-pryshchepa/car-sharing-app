package car.sharing.app.dto.car;

import car.sharing.app.model.TypeName;
import java.math.BigDecimal;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CarDto {
    private Long id;
    private String model;
    private String brand;
    private TypeName name;
    private int inventory;
    private BigDecimal dailyFee;
}
