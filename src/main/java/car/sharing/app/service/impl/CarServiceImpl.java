package car.sharing.app.service.impl;

import car.sharing.app.dto.car.CarDto;
import car.sharing.app.dto.car.CreateCarRequestDto;
import car.sharing.app.exception.EntityNotFoundException;
import car.sharing.app.mapper.CarMapper;
import car.sharing.app.model.Car;
import car.sharing.app.repository.car.CarRepository;
import car.sharing.app.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    @Override
    public CarDto save(CreateCarRequestDto requestDto) {
        Car car = carMapper.toEntity(requestDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public Page<CarDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    public CarDto findById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + id)
        );
        return carMapper.toDto(car);
    }

    @Override
    public CarDto update(Long id, CreateCarRequestDto requestDto) {
        Car car = carRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find car by id: " + id)
        );
        carMapper.updateCarFromDto(requestDto, car);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public void deleteById(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find car by id: " + id);
        }
        carRepository.deleteById(id);
    }
}
