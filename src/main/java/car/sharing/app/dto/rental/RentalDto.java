package car.sharing.app.dto.rental;

import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RentalDto {
    private Long id;
    private LocalDate rentalDate;
    private LocalDate returnDate;
    private LocalDate actualReturnDate;

    private Long carId;
    private String carModel;
    private String carBrand;

    private Long userId;
    private String userEmail;
}
