package car.sharing.app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import car.sharing.app.model.Rental;
import car.sharing.app.repository.rental.RentalRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RentalRepositoryTest {

    @Autowired
    private RentalRepository rentalRepository;

    @Test
    @Sql(scripts = {
            "classpath:database/delete-rentals.sql",
            "classpath:database/insert-rentals.sql"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "classpath:database/delete-rentals.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void testFindAllByUserId() {
        List<Rental> rentals = rentalRepository.findAllByUserId(1L);
        assertEquals(2, rentals.size());
    }
}
