package car.sharing.app.repository.rental;

import car.sharing.app.model.Rental;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findAllByUserId(Long userId);
}
