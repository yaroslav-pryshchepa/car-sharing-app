package car.sharing.app.service;

import car.sharing.app.dto.user.UpdateUserProfileRequestDto;
import car.sharing.app.dto.user.UpdateUserRoleRequestDto;
import car.sharing.app.dto.user.UserRegistrationRequestDto;
import car.sharing.app.dto.user.UserResponseDto;
import car.sharing.app.exception.RegistrationException;

public interface UserService {
    UserResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    Long getCurrentUserId();

    UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequestDto requestDto);

    UserResponseDto getCurrentUserProfile();

    UserResponseDto updateCurrentUserProfile(UpdateUserProfileRequestDto requestDto);
}
