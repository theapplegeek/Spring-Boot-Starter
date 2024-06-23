package it.theapplegeek.spring_starter_pack.service;

import it.theapplegeek.spring_starter_pack.dto.UserDto;
import it.theapplegeek.spring_starter_pack.error.role.RoleMessage;
import it.theapplegeek.spring_starter_pack.error.user.UserMessage;
import it.theapplegeek.spring_starter_pack.exception.BadRequestException;
import it.theapplegeek.spring_starter_pack.exception.NotFoundException;
import it.theapplegeek.spring_starter_pack.mapper.UserMapper;
import it.theapplegeek.spring_starter_pack.model.Role;
import it.theapplegeek.spring_starter_pack.model.User;
import it.theapplegeek.spring_starter_pack.repository.RoleRepository;
import it.theapplegeek.spring_starter_pack.repository.UserRepository;
import it.theapplegeek.spring_starter_pack.util.pagination.PagedListDto;
import it.theapplegeek.spring_starter_pack.util.pagination.PagedListMapper;
import it.theapplegeek.spring_starter_pack.util.pagination.PagedRequestParams;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PagedListMapper<UserDto, User> userPagedMapper;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final AuthService authService;

  public PagedListDto<UserDto> getAllUsers(
      int page, int size, String sort, String direction, UserDto search) {
    Page<User> users =
        userRepository.findAll(
            search,
            PagedRequestParams.builder()
                .page(page)
                .size(size)
                .sort(sort)
                .direction(direction)
                .build()
                .asPageable());
    return userPagedMapper.toDto(users, userMapper);
  }

  public UserDto addUser(UserDto userDto) {
    if (userRepository.existsByUsername(userDto.getUsername())) {
      throw new BadRequestException(UserMessage.USERNAME_ALREADY_EXISTS);
    }
    if (userRepository.existsByEmail(userDto.getEmail())) {
      throw new BadRequestException(UserMessage.EMAIL_ALREADY_EXISTS);
    }

    User user = userMapper.toEntity(userDto);
    if (userDto.getRole() != null && userDto.getRole().getId() != null) {
      Role role =
          roleRepository
              .findById(userDto.getRole().getId())
              .orElseThrow(() -> new NotFoundException(RoleMessage.ROLE_NOT_FOUND));
      user.setRole(role);
    } else {
      Role userRole =
          roleRepository
              .findByName("user")
              .orElseThrow(() -> new NotFoundException(RoleMessage.ROLE_NOT_FOUND));
      user.setRoleId(userRole.getId());
      user.setRole(userRole);
    }
    if (userDto.getEnabled() == null) user.setEnabled(true);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userMapper.toDto(userRepository.save(user));
  }

  public UserDto updateUser(Long userId, UserDto userDto) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException(UserMessage.USER_NOT_FOUND));
    if (userDto.getUsername() != null
        && userRepository.existsByUsername(userDto.getUsername())
        && !user.getUsername().equals(userDto.getUsername())) {
      throw new BadRequestException(UserMessage.USERNAME_ALREADY_EXISTS);
    }
    if (userDto.getEmail() != null
        && userRepository.existsByEmail(userDto.getEmail())
        && !user.getEmail().equals(userDto.getEmail())) {
      throw new BadRequestException(UserMessage.EMAIL_ALREADY_EXISTS);
    }
    if (userDto.getRole() != null && userDto.getRole().getId() != null) {
      Role role =
          roleRepository
              .findById(userDto.getRole().getId())
              .orElseThrow(() -> new NotFoundException(RoleMessage.ROLE_NOT_FOUND));
      user.setRole(role);
    }
    user = userMapper.partialUpdate(userDto, user);
    User userUpdated = userRepository.save(user);
    if (userDto.getUsername() != null && !user.getUsername().equals(userDto.getUsername()))
      authService.revokeAllTokensOfUser(user.getId());
    return userMapper.toDto(userUpdated);
  }

  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException(UserMessage.USER_NOT_FOUND));
    userRepository.delete(user);
  }
}
