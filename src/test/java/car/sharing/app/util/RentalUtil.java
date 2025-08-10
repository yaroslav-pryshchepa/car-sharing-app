package car.sharing.app.util;

import static car.sharing.app.util.UserUtil.createUser;

import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import car.sharing.app.model.Car;
import car.sharing.app.model.Rental;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RentalUtil {
    public static RentalDto createRentalDto(Long id) {
        return new RentalDto()
                .setId(id)
                .setRentalDate(LocalDate.of(2025, 7, 1))
                .setReturnDate(LocalDate.of(2025, 7, 5))
                .setActualReturnDate(null)
                .setCarId(1L)
                .setCarModel("Model S")
                .setCarBrand("Tesla")
                .setUserId(1L)
                .setUserEmail("test@example.com");
    }

    public static Rental createRental(Long id) {
        return new Rental()
                .setId(2L)
                .setUser(createUser(1L))
                .setCar(new Car().setDailyFee(BigDecimal.valueOf(100)))
                .setRentalDate(LocalDate.now().minusDays(2))
                .setReturnDate(LocalDate.now());
    }

    public static CreateRentalRequestDto createRentalRequestDto() {
        return new CreateRentalRequestDto()
                .setCarId(1L)
                .setUserId(1L)
                .setReturnDate(LocalDate.of(2025, 7, 5));
    }

    public static CreateRentalRequestDto createBadRentalRequestDto() {
        return new CreateRentalRequestDto()
                .setCarId(null)
                .setUserId(null)
                .setReturnDate(null);
    }
}
