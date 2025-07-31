package car.sharing.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import car.sharing.app.model.Car;
import car.sharing.app.repository.car.CarRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @Test
    @Sql(scripts = {
            "classpath:database/delete-cars.sql",
            "classpath:database/insert-cars.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/delete-cars.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindAllCars() {
        List<Car> cars = carRepository.findAll();
        assertEquals(3, cars.size());
    }
}
