package car.sharing.app.util;

import car.sharing.app.dto.user.UpdateUserProfileRequestDto;
import car.sharing.app.dto.user.UpdateUserRoleRequestDto;
import car.sharing.app.dto.user.UserLoginRequestDto;
import car.sharing.app.dto.user.UserRegistrationRequestDto;
import car.sharing.app.dto.user.UserResponseDto;

public class UserUtil {

    public static UserResponseDto createUserResponseDto(Long id) {
        return new UserResponseDto()
                .setId(id)
                .setEmail("test@example.com")
                .setFirstName("Test")
                .setLastName("User");
    }

    public static UserResponseDto createUpdatedUserResponseDto(Long id) {
        return new UserResponseDto()
                .setId(id)
                .setEmail("updated@example.com")
                .setFirstName("Updated")
                .setLastName("User");
    }

    public static UpdateUserRoleRequestDto createUpdateUserRoleRequestDto(String role) {
        UpdateUserRoleRequestDto dto = new UpdateUserRoleRequestDto();
        dto.setRole(role);
        return dto;
    }

    public static UpdateUserProfileRequestDto createUpdateUserRoleRequestDtoWithEmptyName() {
        return new UpdateUserProfileRequestDto()
                .setFirstName("")
                .setLastName("ValidLastName");
    }

    public static UpdateUserProfileRequestDto createUpdateUserProfileRequestDto() {
        UpdateUserProfileRequestDto dto = new UpdateUserProfileRequestDto();
        dto.setEmail("updated@example.com");
        dto.setFirstName("Updated");
        dto.setLastName("User");
        return dto;
    }

    public static UpdateUserRoleRequestDto createUpdateUserInvalidRoleRequestDto() {
        return new UpdateUserRoleRequestDto()
                .setRole(null);
    }

    public static UserRegistrationRequestDto createRegistrationRequestWithShortPassword() {
        return new UserRegistrationRequestDto()
                .setEmail("user@example.com")
                .setFirstName("John")
                .setLastName("Doe")
                .setPassword("123")
                .setRepeatPassword("123");
    }

    public static UserResponseDto createUserResponseDtoWithShortPassword(Long id) {
        return new UserResponseDto()
                .setId(id)
                .setEmail("user@example.com")
                .setFirstName("John")
                .setLastName("Doe");
    }

    public static UserRegistrationRequestDto createEmptyRegistrationRequest() {
        return new UserRegistrationRequestDto();
    }

    public static UserLoginRequestDto createEmptyLoginRequest() {
        return new UserLoginRequestDto();
    }

    public static UserLoginRequestDto createLoginRequestWithInvalidEmailFormat() {
        return new UserLoginRequestDto()
                .setEmail("invalid-email")
                .setPassword("somePassword123");
    }

    public static UserLoginRequestDto createLoginRequest() {
        return new UserLoginRequestDto()
                .setEmail("test@example.com")
                .setPassword("password");
    }

}
