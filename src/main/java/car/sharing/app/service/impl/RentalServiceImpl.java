package car.sharing.app.service.impl;

import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import car.sharing.app.exception.EntityNotFoundException;
import car.sharing.app.mapper.RentalMapper;
import car.sharing.app.model.Car;
import car.sharing.app.model.Rental;
import car.sharing.app.model.User;
import car.sharing.app.repository.car.CarRepository;
import car.sharing.app.repository.rental.RentalRepository;
import car.sharing.app.repository.user.UserRepository;
import car.sharing.app.service.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;

    @Override
    @Transactional
    public RentalDto create(CreateRentalRequestDto requestDto) {
        Car car = carRepository.findById(requestDto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Car not found with id: " + requestDto.getCarId()));
        if (car.getInventory() == 0) {
            throw new IllegalStateException("No available cars in inventory.");
        }

        User user = userRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + requestDto.getUserId()));

        Rental rental = rentalMapper.toEntity(requestDto)
                .setCar(car)
                .setUser(user)
                .setRentalDate(LocalDate.now());

        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);
        rentalRepository.save(rental);
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public List<RentalDto> findAllByUserIdAndIsActive(Long userId, boolean isActive) {
        LocalDate today = LocalDate.now();

        return rentalRepository.findAllByUserId(userId).stream()
                .filter(rental -> {
                    boolean currentlyActive =
                            rental.getActualReturnDate() == null
                                    && !today.isBefore(rental.getRentalDate())
                                    && !today.isAfter(rental.getReturnDate());

                    return isActive == currentlyActive;
                })
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public RentalDto findById(Long id) {
        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rental not found with id: " + id));
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalDto returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Rental not found with id: " + rentalId));

        if (rental.getActualReturnDate() != null) {
            throw new IllegalStateException("Rental already returned");
        }

        rental.setActualReturnDate(LocalDate.now());
        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);
        System.out.println(rental.getActualReturnDate());
        rentalRepository.save(rental);
        return rentalMapper.toDto(rental);
    }
}

