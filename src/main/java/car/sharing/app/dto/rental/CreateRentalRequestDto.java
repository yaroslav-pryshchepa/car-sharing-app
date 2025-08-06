package car.sharing.app.dto.rental;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CreateRentalRequestDto {

    @NotNull
    private LocalDate returnDate;
    @NotNull
    private Long carId;
    @NotNull
    @Positive
    private Long userId;
}
