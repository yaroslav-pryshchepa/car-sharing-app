package car.sharing.app.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRoleRequestDto {
    @NotBlank(message = "Wrong role")
    private String role;
}
