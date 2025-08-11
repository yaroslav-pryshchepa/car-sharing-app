package car.sharing.app.mapper;

import car.sharing.app.config.MapperConfig;
import car.sharing.app.dto.rental.CreateRentalRequestDto;
import car.sharing.app.dto.rental.RentalDto;
import car.sharing.app.model.Rental;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {

    @Mapping(source = "car.id", target = "carId")
    @Mapping(source = "car.model", target = "carModel")
    @Mapping(source = "car.brand", target = "carBrand")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "userEmail")
    RentalDto toDto(Rental rental);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "actualReturnDate", ignore = true)
    @Mapping(target = "car", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Rental toEntity(CreateRentalRequestDto requestDto);
}
