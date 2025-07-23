package car.sharing.app.repository.car;

import car.sharing.app.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarRepository extends JpaRepository<Car, Long>,
        JpaSpecificationExecutor<Car> {
}
