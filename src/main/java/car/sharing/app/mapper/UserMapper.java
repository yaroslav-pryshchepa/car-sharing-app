package car.sharing.app.mapper;

import car.sharing.app.config.MapperConfig;
import car.sharing.app.dto.user.UserRegistrationRequestDto;
import car.sharing.app.dto.user.UserResponseDto;
import car.sharing.app.model.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface UserMapper {

    UserResponseDto toDto(User user);

    User toModel(UserRegistrationRequestDto requestDto);
}
