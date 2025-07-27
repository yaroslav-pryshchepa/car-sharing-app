package car.sharing.app.service.impl;

import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import car.sharing.app.exception.EntityNotFoundException;
import car.sharing.app.mapper.RentalMapper;
import car.sharing.app.model.Car;
import car.sharing.app.model.Rental;
import car.sharing.app.model.RoleName;
import car.sharing.app.model.User;
import car.sharing.app.repository.car.CarRepository;
import car.sharing.app.repository.rental.RentalRepository;
import car.sharing.app.repository.user.UserRepository;
import car.sharing.app.service.NotificationService;
import car.sharing.app.service.RentalService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final RentalMapper rentalMapper;
    private final NotificationService notificationService;

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
        notificationService.sendMessage(String.format(
                "🚗 Створено нову аренду!\n"
                        + "Користувач: %s\n"
                        + "Машина: %s\n"
                        + "Дата початку аренди: %s\n"
                        + "Дата повернення: %s",
                user.getEmail(), car.getModel(), rental.getRentalDate(), rental.getReturnDate()
        ));
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RentalDto> getRentals(Long userId, Boolean isActive,
            Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        boolean isManager = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.MANAGER);

        if (!isManager && userId != null && !userId.equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to view rentals of another user.");
        }

        if (!isManager && userId == null) {
            userId = currentUser.getId();
        }

        List<Rental> rentals = (userId != null)
                ? rentalRepository.findAllByUserId(userId)
                : rentalRepository.findAll();

        LocalDate today = LocalDate.now();
        return rentals.stream()
                .filter(rental -> {
                    if (isActive == null) {
                        return true;
                    }
                    boolean currentlyActive = rental.getActualReturnDate() == null
                            && !today.isBefore(rental.getRentalDate())
                            && !today.isAfter(rental.getReturnDate());
                    return isActive == currentlyActive;
                })
                .map(rentalMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RentalDto findById(Long id, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        boolean isManager = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.MANAGER);

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental not found with id: " + id));

        if (!isManager && !rental.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to view this rental.");
        }

        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalDto returnRental(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Rental not found with id: " + rentalId));

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
