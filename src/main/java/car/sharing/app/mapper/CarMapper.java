package car.sharing.app.mapper;

import car.sharing.app.config.MapperConfig;
import car.sharing.app.dto.car.CarDto;
import car.sharing.app.dto.car.CreateCarRequestDto;
import car.sharing.app.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {

    CarDto toDto(Car car);

    Car toEntity(CreateCarRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateCarFromDto(CreateCarRequestDto requestDto, @MappingTarget Car car);
}
