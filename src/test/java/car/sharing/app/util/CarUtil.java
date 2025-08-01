package car.sharing.app.util;

import car.sharing.app.dto.car.CarDto;
import car.sharing.app.dto.car.CreateCarRequestDto;
import car.sharing.app.model.Car;
import car.sharing.app.model.TypeName;
import java.math.BigDecimal;
import java.util.List;

public class CarUtil {
    public static CarDto createCarDto(Long id) {
        return new CarDto()
                .setId(id)
                .setBrand("Tesla")
                .setModel("Model S")
                .setTypeName(TypeName.SEDAN)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(100.00));
    }

    public static List<Car> createListOfCars() {
        Car car1 = new Car()
                .setId(1L)
                .setModel("Model S")
                .setBrand("Tesla")
                .setTypeName(TypeName.SEDAN)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(100.00))
                .setDeleted(false);

        Car car2 = new Car()
                .setId(2L)
                .setModel("Civic")
                .setBrand("Honda")
                .setTypeName(TypeName.SEDAN)
                .setInventory(3)
                .setDailyFee(BigDecimal.valueOf(40.00))
                .setDeleted(false);

        Car car3 = new Car()
                .setId(3L)
                .setModel("Rav4")
                .setBrand("Toyota")
                .setTypeName(TypeName.SEDAN)
                .setInventory(4)
                .setDailyFee(BigDecimal.valueOf(60.00))
                .setDeleted(false);

        return List.of(car1, car2, car3);
    }

    public static CreateCarRequestDto createCarRequestDto() {
        return new CreateCarRequestDto()
                .setBrand("Tesla")
                .setModel("Model S")
                .setTypeName(TypeName.SEDAN)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(100.00));
    }

    public static CreateCarRequestDto createUpdatedCarRequestDto() {
        return new CreateCarRequestDto()
                .setModel("Updated Model")
                .setBrand("Toyota")
                .setTypeName(TypeName.HATCHBACK)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(49.99));
    }

    public static CarDto createUpdatedCarDto(Long id) {
        return new CarDto()
                .setId(id)
                .setModel("Updated Model")
                .setBrand("Toyota")
                .setTypeName(TypeName.HATCHBACK)
                .setInventory(5)
                .setDailyFee(BigDecimal.valueOf(49.99));
    }

    public static CreateCarRequestDto createCarBadRequestDto(Long id) {
        return new CreateCarRequestDto()
                .setModel("")
                .setBrand("")
                .setTypeName(null)
                .setInventory(-1)
                .setDailyFee(BigDecimal.valueOf(-5));
    }
}
