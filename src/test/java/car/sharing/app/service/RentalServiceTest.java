package car.sharing.app.service;

import static car.sharing.app.util.CarUtil.createCar;
import static car.sharing.app.util.RentalUtil.createRental;
import static car.sharing.app.util.RentalUtil.createRentalDto;
import static car.sharing.app.util.RentalUtil.createRentalRequestDto;
import static car.sharing.app.util.UserUtil.createManagerUser;
import static car.sharing.app.util.UserUtil.createUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import car.sharing.app.service.impl.RentalServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RentalMapper rentalMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RentalServiceImpl rentalService;

    private Car car;
    private User user;
    private Rental rental;
    private RentalDto rentalDto;

    @BeforeEach
    void setUp() {
        car = createCar(2L);
        user = createUser(1L);
        rental = createRental(10L);
        rentalDto = createRentalDto(10L);
    }

    @Test
    void createRental_success() {
        CreateRentalRequestDto requestDto = createRentalRequestDto();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(rentalMapper.toEntity(requestDto)).thenReturn(new Rental());
        when(rentalMapper.toDto(any(Rental.class))).thenReturn(rentalDto);

        RentalDto result = rentalService.create(requestDto);

        assertNotNull(result);
        verify(carRepository).save(car);
        verify(rentalRepository).save(any(Rental.class));
        verify(notificationService).sendMessage(contains("Створено нову аренду"));
    }

    @Test
    void createRental_noCarInInventory_throwsException() {
        car.setInventory(0);
        CreateRentalRequestDto requestDto = createRentalRequestDto();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        assertThrows(IllegalStateException.class,
                () -> rentalService.create(requestDto));
    }

    @Test
    void createRental_userNotFound_throwsException() {
        CreateRentalRequestDto requestDto = createRentalRequestDto();

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> rentalService.create(requestDto));
    }

    @Test
    void getRentals_asManager_returnsAll() {
        User manager = createManagerUser(1L);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(manager);

        when(rentalRepository.findAll()).thenReturn(List.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        List<RentalDto> result = rentalService.getRentals(null, null, auth);

        assertEquals(1, result.size());
    }

    @Test
    void getRentals_customerTryingOtherUser_throwsAccessDenied() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        assertThrows(AccessDeniedException.class,
                () -> rentalService.getRentals(99L, null, auth));
    }

    @Test
    void findById_customerOwnRental_success() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        RentalDto result = rentalService.findById(10L, auth);

        assertNotNull(result);
    }

    @Test
    void findById_customerOtherRental_throwsAccessDenied() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);

        User otherUser = new User().setId(99L);
        rental.setUser(otherUser);

        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));

        assertThrows(AccessDeniedException.class,
                () -> rentalService.findById(10L, auth));
    }

    @Test
    void returnRental_success() {
        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));
        when(rentalMapper.toDto(rental)).thenReturn(rentalDto);

        RentalDto result = rentalService.returnRental(10L);

        assertNotNull(result);
        assertEquals(2, car.getInventory());
        verify(carRepository).save(any(Car.class));
        assertEquals(2, car.getInventory());
        verify(rentalRepository).save(rental);
    }

    @Test
    void returnRental_alreadyReturned_throwsException() {
        rental.setActualReturnDate(LocalDate.now());
        when(rentalRepository.findById(10L)).thenReturn(Optional.of(rental));

        assertThrows(IllegalStateException.class,
                () -> rentalService.returnRental(10L));
    }

    @Test
    void notifyOverdueRentals_noneOverdue_sendsNoOverdueMessage() {
        rental.setActualReturnDate(LocalDate.now());
        when(rentalRepository.findAll()).thenReturn(List.of(rental));

        rentalService.notifyOverdueRentals();

        verify(notificationService).sendMessage(contains("немає простроченої"));
    }

    @Test
    void notifyOverdueRentals_withOverdue_sendsOverdueMessage() {
        rental.setActualReturnDate(null);
        rental.setReturnDate(LocalDate.now().minusDays(1));
        when(rentalRepository.findAll()).thenReturn(List.of(rental));

        rentalService.notifyOverdueRentals();

        verify(notificationService).sendMessage(contains("прострочену оренду"));
    }
}

