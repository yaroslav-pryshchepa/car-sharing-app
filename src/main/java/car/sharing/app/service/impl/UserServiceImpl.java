package car.sharing.app.service.impl;

import car.sharing.app.dto.user.UpdateUserProfileRequestDto;
import car.sharing.app.dto.user.UpdateUserRoleRequestDto;
import car.sharing.app.dto.user.UserRegistrationRequestDto;
import car.sharing.app.dto.user.UserResponseDto;
import car.sharing.app.exception.EntityNotFoundException;
import car.sharing.app.exception.RegistrationException;
import car.sharing.app.mapper.UserMapper;
import car.sharing.app.model.Role;
import car.sharing.app.model.RoleName;
import car.sharing.app.model.User;
import car.sharing.app.repository.role.RoleRepository;
import car.sharing.app.repository.user.UserRepository;
import car.sharing.app.service.UserService;
import jakarta.transaction.Transactional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder getPasswordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new RegistrationException("Email already in use: " + requestDto.getEmail());
        }
        User user = userMapper.toModel(requestDto);
        user.setPassword(getPasswordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: "
                        + RoleName.CUSTOMER));
        user.setRoles(Set.of(userRole));
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public Long getCurrentUserId() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return user.getId();
    }

    @Override
    public UserResponseDto updateUserRole(Long userId, UpdateUserRoleRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));
        Role role = roleRepository.findByName(RoleName.valueOf(requestDto.getRole()))
                .orElseThrow(() -> new EntityNotFoundException("Role not found: "
                        + requestDto.getRole()));
        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getCurrentUserProfile() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found by id: " + userId));
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto updateCurrentUserProfile(UpdateUserProfileRequestDto requestDto) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found by id: " + userId));
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        user.setEmail(requestDto.getEmail());
        userRepository.save(user);
        return userMapper.toDto(user);
    }
}
