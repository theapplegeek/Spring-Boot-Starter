package it.theapplegeek.spring_starter_pack.user.service;

import it.theapplegeek.spring_starter_pack.security.model.UserLogged;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.role.error.RoleMessage;
import it.theapplegeek.spring_starter_pack.user.error.UserMessage;
import it.theapplegeek.spring_starter_pack.common.exception.BadRequestException;
import it.theapplegeek.spring_starter_pack.common.exception.NotFoundException;
import it.theapplegeek.spring_starter_pack.user.mapper.UserMapper;
import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.role.repository.RoleRepository;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedListDto;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedListMapper;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedRequestParams;
import it.theapplegeek.spring_starter_pack.auth.service.AuthService;
import jakarta.transaction.Transactional;
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

  @Transactional
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

  @Transactional
  public void changePassword(UserLogged userLogged, String oldPassword, String newPassword) {
    User user =
        userRepository
            .findById(userLogged.getId())
            .orElseThrow(() -> new NotFoundException(UserMessage.USER_NOT_FOUND));

    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
      throw new BadRequestException(UserMessage.INVALID_OLD_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(user);
    authService.revokeAllTokensOfUser(user.getId());
  }

  public void deleteUser(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new NotFoundException(UserMessage.USER_NOT_FOUND));
    userRepository.delete(user);
  }
}
