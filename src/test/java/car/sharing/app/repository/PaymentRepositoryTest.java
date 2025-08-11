package car.sharing.app.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import car.sharing.app.model.Payment;
import car.sharing.app.model.PaymentStatus;
import car.sharing.app.model.PaymentType;
import car.sharing.app.repository.payment.PaymentRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @Sql(scripts = {
            "classpath:database/delete-payments.sql",
            "classpath:database/delete-rentals.sql",
            "classpath:database/delete-cars.sql",
            "classpath:database/delete-users-and-roles.sql",
            "classpath:database/insert-users-and-roles.sql",
            "classpath:database/insert-cars.sql",
            "classpath:database/insert-rentals.sql",
            "classpath:database/insert-payments.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/delete-payments.sql",
            "classpath:database/delete-rentals.sql",
            "classpath:database/delete-cars.sql",
            "classpath:database/delete-users-and-roles.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testExistsByRentalIdAndPaymentTypeAndStatus() {
        boolean exists = paymentRepository.existsByRentalIdAndPaymentTypeAndStatus(
                1L, PaymentType.PAYMENT, PaymentStatus.PAID
        );
        assertTrue(exists);
    }

    @Test
    @Sql(scripts = {
            "classpath:database/delete-payments.sql",
            "classpath:database/delete-rentals.sql",
            "classpath:database/delete-cars.sql",
            "classpath:database/delete-users-and-roles.sql",
            "classpath:database/insert-users-and-roles.sql",
            "classpath:database/insert-cars.sql",
            "classpath:database/insert-rentals.sql",
            "classpath:database/insert-payments.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/delete-payments.sql",
            "classpath:database/delete-rentals.sql",
            "classpath:database/delete-cars.sql",
            "classpath:database/delete-users-and-roles.sql"
    },
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindBySessionId() {
        Optional<Payment> payment = paymentRepository.findBySessionId("sess_1");
        assertTrue(payment.isPresent());
    }
}

