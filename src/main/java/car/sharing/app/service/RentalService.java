package car.sharing.app.service;

import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import java.util.List;
import org.springframework.security.core.Authentication;

public interface RentalService {
    RentalDto create(CreateRentalRequestDto requestDto);

    List<RentalDto> getRentals(Long userId, Boolean isActive, Authentication authentication);

    RentalDto findById(Long id, Authentication authentication);

    RentalDto returnRental(Long rentalId);
}
