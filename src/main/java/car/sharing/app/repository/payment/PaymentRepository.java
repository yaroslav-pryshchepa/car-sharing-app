package car.sharing.app.repository.payment;

import car.sharing.app.model.Payment;
import car.sharing.app.model.PaymentStatus;
import car.sharing.app.model.PaymentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByRentalIdIn(List<Long> rentalIds);

    Optional<Payment> findBySessionId(String sessionId);

    boolean existsByRentalIdAndPaymentTypeAndStatus(
            Long rentalId,
            PaymentType paymentType,
            PaymentStatus status
    );
}
