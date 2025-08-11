package car.sharing.app.service;

import static car.sharing.app.util.CarUtil.createCarDto;
import static car.sharing.app.util.CarUtil.createCarRequestDto;
import static car.sharing.app.util.CarUtil.createListOfCars;
import static car.sharing.app.util.CarUtil.createUpdatedCarDto;
import static car.sharing.app.util.CarUtil.createUpdatedCarRequestDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import car.sharing.app.dto.car.CarDto;
import car.sharing.app.dto.car.CreateCarRequestDto;
import car.sharing.app.exception.EntityNotFoundException;
import car.sharing.app.mapper.CarMapper;
import car.sharing.app.model.Car;
import car.sharing.app.repository.car.CarRepository;
import car.sharing.app.service.impl.CarServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @InjectMocks
    private CarServiceImpl carService;
    @Mock
    private CarRepository carRepository;
    @Mock
    private CarMapper carMapper;

    @Test
    @DisplayName("save should save and return CarDto")
    void save_returnsSavedCarDto() {
        CreateCarRequestDto requestDto = createCarRequestDto();
        Car car = createListOfCars().get(0);
        CarDto carDto = createCarDto(1L);

        when(carMapper.toEntity(requestDto)).thenReturn(car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(carDto);

        CarDto result = carService.save(requestDto);

        assertNotNull(result);
        assertEquals(carDto, result);
        verify(carMapper).toEntity(requestDto);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("findAll should return page of CarDto")
    void findAll_returnsPageOfCarDto() {
        Pageable pageable = PageRequest.of(0, 10);
        Car car = createListOfCars().get(0);
        CarDto carDto = createCarDto(1L);
        Page<Car> carPage = new PageImpl<>(List.of(car));

        when(carRepository.findAll(pageable)).thenReturn(carPage);
        when(carMapper.toDto(car)).thenReturn(carDto);

        Page<CarDto> result = carService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(carDto, result.getContent().get(0));
        verify(carRepository).findAll(pageable);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("findById should return CarDto when car exists")
    void findById_existingCar_returnsCarDto() {
        Long id = 1L;
        Car car = createListOfCars().get(0);
        CarDto carDto = createCarDto(id);

        when(carRepository.findById(id)).thenReturn(Optional.of(car));
        when(carMapper.toDto(car)).thenReturn(carDto);

        CarDto result = carService.findById(id);

        assertNotNull(result);
        assertEquals(carDto, result);
        verify(carRepository).findById(id);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("findById should throw if car not found")
    void findById_carNotFound_throwsException() {
        Long id = 1L;
        when(carRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> carService.findById(id));
        assertEquals("Can't find car by id: " + id, ex.getMessage());
        verify(carRepository).findById(id);
        verifyNoMoreInteractions(carMapper);
    }

    @Test
    @DisplayName("update should update and return CarDto")
    void update_existingCar_returnsUpdatedCarDto() {
        Long id = 1L;
        CreateCarRequestDto requestDto = createUpdatedCarRequestDto();
        Car car = createListOfCars().get(0);
        CarDto updatedDto = createUpdatedCarDto(id);

        when(carRepository.findById(id)).thenReturn(Optional.of(car));
        doNothing().when(carMapper).updateCarFromDto(requestDto, car);
        when(carRepository.save(car)).thenReturn(car);
        when(carMapper.toDto(car)).thenReturn(updatedDto);

        CarDto result = carService.update(id, requestDto);

        assertNotNull(result);
        assertEquals(updatedDto, result);
        verify(carRepository).findById(id);
        verify(carMapper).updateCarFromDto(requestDto, car);
        verify(carRepository).save(car);
        verify(carMapper).toDto(car);
    }

    @Test
    @DisplayName("update should throw if car not found")
    void update_carNotFound_throwsException() {
        Long id = 1L;
        CreateCarRequestDto requestDto = createUpdatedCarRequestDto();
        when(carRepository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> carService.update(id, requestDto));
        assertEquals("Can't find car by id: " + id, ex.getMessage());
        verify(carRepository).findById(id);
        verifyNoMoreInteractions(carMapper, carRepository);
    }

    @Test
    @DisplayName("deleteById should call repository when car exists")
    void deleteById_callsRepository() {
        Long id = 1L;
        when(carRepository.existsById(id)).thenReturn(true);
        doNothing().when(carRepository).deleteById(id);

        carService.deleteById(id);

        verify(carRepository).existsById(id);
        verify(carRepository).deleteById(id);
    }

    @Test
    @DisplayName("deleteById should throw if car not found")
    void deleteById_carNotFound_throwsException() {
        Long id = 1L;
        when(carRepository.existsById(id)).thenReturn(false);

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> carService.deleteById(id));
        assertEquals("Can't find car by id: " + id, ex.getMessage());
        verify(carRepository).existsById(id);
        verifyNoMoreInteractions(carRepository);
    }
}
