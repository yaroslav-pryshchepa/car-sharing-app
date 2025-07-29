package car.sharing.app.repository.payment;

import car.sharing.app.model.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByRentalIdIn(List<Long> rentalIds);
}
