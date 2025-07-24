package car.sharing.app.service;

import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import java.util.List;

public interface RentalService {
    RentalDto create(CreateRentalRequestDto requestDto);

    List<RentalDto> findAllByUserIdAndIsActive(Long userId, boolean isActive);

    RentalDto findById(Long id);

    RentalDto returnRental(Long rentalId);
}
