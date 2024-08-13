package it.theapplegeek.spring_starter_pack.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.github.javafaker.Faker;
import it.theapplegeek.spring_starter_pack.auth.service.AuthService;
import it.theapplegeek.spring_starter_pack.common.exception.BadRequestException;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedListMapper;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedRequestParams;
import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.role.repository.RoleRepository;
import it.theapplegeek.spring_starter_pack.security.model.UserLogged;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.error.UserMessage;
import it.theapplegeek.spring_starter_pack.user.mapper.UserMapper;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock UserRepository userRepository;
  @Mock RoleRepository roleRepository;
  @Mock PagedListMapper<UserDto, User> userPagedMapper;
  @Mock UserMapper userMapper;
  @Mock PasswordEncoder passwordEncoder;
  @Mock AuthService authService;
  @InjectMocks UserService userService;

  Faker faker = new Faker();

  @Test
  void canGetAllUsers() {
    // given
    int page = 0;
    int size = 10;
    String sort = "username";
    String direction = "asc";
    UserDto userDto = UserDto.builder().build();
    Pageable pageable =
        PagedRequestParams.builder()
            .page(page)
            .size(size)
            .sort(sort)
            .direction(direction)
            .build()
            .asPageable();

    // when
    userService.getAllUsers(page, size, sort, direction, userDto);

    // then
    verify(userRepository, times(1)).findAll(userDto, pageable);
    verify(userPagedMapper, times(1)).toDto(any(), any(UserMapper.class));
  }

  @Test
  void canAddUserWithRole() {
    // given
    UserDto userDto =
        UserDto.builder()
            .username(faker.name().username())
            .email(faker.internet().emailAddress())
            .name(faker.name().firstName())
            .surname(faker.name().lastName())
            .password(faker.internet().password())
            .role(RoleDto.builder().id(1L).build())
            .build();
    User user =
        User.builder()
            .username(userDto.getUsername())
            .email(userDto.getEmail())
            .name(userDto.getName())
            .surname(userDto.getSurname())
            .build();
    Role role = Role.builder().id(1L).name("user").build();

    given(userMapper.toEntity(userDto)).willReturn(user);
    given(roleRepository.findById(role.getId())).willReturn(Optional.of(role));
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    userService.addUser(userDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // then
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
    verify(userMapper, times(1)).toEntity(userDto);
    verify(roleRepository, times(1)).findById(userDto.getRole().getId());
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(userMapper, times(1)).toDto(user);

    assertEquals(userDto.getUsername(), userCaptor.getValue().getUsername());
    assertEquals(userDto.getEmail(), userCaptor.getValue().getEmail());
    assertEquals(userDto.getName(), userCaptor.getValue().getName());
    assertEquals(userDto.getSurname(), userCaptor.getValue().getSurname());
    assertEquals(
        passwordEncoder.encode(userDto.getPassword()), userCaptor.getValue().getPassword());
    assertEquals(role, userCaptor.getValue().getRole());
    assertTrue(userCaptor.getValue().getEnabled());
  }

  @Test
  void canAddUserWithDefaultRole() {
    // given
    UserDto userDto =
        UserDto.builder()
            .username(faker.name().username())
            .email(faker.internet().emailAddress())
            .name(faker.name().firstName())
            .surname(faker.name().lastName())
            .password(faker.internet().password())
            .enabled(false)
            .build();
    User user =
        User.builder()
            .username(userDto.getUsername())
            .email(userDto.getEmail())
            .name(userDto.getName())
            .surname(userDto.getSurname())
            .enabled(userDto.getEnabled())
            .build();
    Role role = Role.builder().id(1L).name("user").build();

    given(userMapper.toEntity(userDto)).willReturn(user);
    given(roleRepository.findByName("user")).willReturn(Optional.of(role));
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    userService.addUser(userDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // then
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
    verify(userMapper, times(1)).toEntity(userDto);
    verify(roleRepository, times(1)).findByName("user");
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(userMapper, times(1)).toDto(user);

    assertEquals(userDto.getUsername(), userCaptor.getValue().getUsername());
    assertEquals(userDto.getEmail(), userCaptor.getValue().getEmail());
    assertEquals(userDto.getName(), userCaptor.getValue().getName());
    assertEquals(userDto.getSurname(), userCaptor.getValue().getSurname());
    assertEquals(
        passwordEncoder.encode(userDto.getPassword()), userCaptor.getValue().getPassword());
    assertEquals(role, userCaptor.getValue().getRole());
    assertFalse(userCaptor.getValue().getEnabled());
  }

  @Test
  void canAddUserWithDefaultRoleWhenRoleIdIsNull() {
    // given
    UserDto userDto =
        UserDto.builder()
            .username(faker.name().username())
            .email(faker.internet().emailAddress())
            .name(faker.name().firstName())
            .surname(faker.name().lastName())
            .password(faker.internet().password())
            .role(RoleDto.builder().build())
            .build();
    User user =
        User.builder()
            .username(userDto.getUsername())
            .email(userDto.getEmail())
            .name(userDto.getName())
            .surname(userDto.getSurname())
            .build();
    Role role = Role.builder().id(1L).name("user").build();

    given(userMapper.toEntity(userDto)).willReturn(user);
    given(roleRepository.findByName("user")).willReturn(Optional.of(role));
    given(userRepository.save(any(User.class))).willReturn(user);

    // when
    userService.addUser(userDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // then
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
    verify(userMapper, times(1)).toEntity(userDto);
    verify(roleRepository, times(1)).findByName("user");
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(userMapper, times(1)).toDto(user);

    assertEquals(userDto.getUsername(), userCaptor.getValue().getUsername());
    assertEquals(userDto.getEmail(), userCaptor.getValue().getEmail());
    assertEquals(userDto.getName(), userCaptor.getValue().getName());
    assertEquals(userDto.getSurname(), userCaptor.getValue().getSurname());
    assertEquals(
        passwordEncoder.encode(userDto.getPassword()), userCaptor.getValue().getPassword());
    assertEquals(role, userCaptor.getValue().getRole());
    assertTrue(userCaptor.getValue().getEnabled());
  }

  @Test
  void cantAddUserWithExistingUsername() {
    // given
    UserDto userDto = UserDto.builder().username(faker.name().username()).build();
    given(userRepository.existsByUsername(userDto.getUsername())).willReturn(true);

    // when
    // then
    assertThrows(
        BadRequestException.class,
        () -> userService.addUser(userDto),
        UserMessage.USERNAME_ALREADY_EXISTS);
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, never()).existsByEmail(anyString());
    verify(userMapper, never()).toEntity(any(UserDto.class));
    verify(roleRepository, never()).findById(anyLong());
    verify(userRepository, never()).save(any(User.class));
    verify(userMapper, never()).toDto(any(User.class));
  }

  @Test
  void cantAddUserWithExistingEmail() {
    // given
    UserDto userDto = UserDto.builder().email(faker.internet().emailAddress()).build();
    given(userRepository.existsByEmail(userDto.getEmail())).willReturn(true);

    // when
    // then
    assertThrows(
        BadRequestException.class,
        () -> userService.addUser(userDto),
        UserMessage.EMAIL_ALREADY_EXISTS);
    verify(userRepository, never()).existsByUsername(anyString());
    verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
    verify(userMapper, never()).toEntity(any(UserDto.class));
    verify(roleRepository, never()).findById(anyLong());
    verify(userRepository, never()).save(any(User.class));
    verify(userMapper, never()).toDto(any(User.class));
  }

  @Test
  void canUpdateUser() {
    // given
    Long userId = 1L;
    UserDto userDto =
        UserDto.builder().name(faker.name().firstName()).surname(faker.name().lastName()).build();
    Role role = Role.builder().id(1L).name("user").build();
    User user =
        User.builder()
            .id(userId)
            .username(faker.name().username())
            .email(faker.internet().emailAddress())
            .name(userDto.getName())
            .surname(userDto.getSurname())
            .password(faker.internet().password())
            .roleId(role.getId())
            .role(role)
            .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userMapper.partialUpdate(userDto, user)).willReturn(user);
    given(userRepository.save(user)).willReturn(user);

    // when
    userService.updateUser(userId, userDto);

    // then
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).existsByUsername(anyString());
    verify(userRepository, never()).existsByEmail(anyString());
    verify(roleRepository, never()).findById(anyLong());
    verify(userMapper, times(1)).partialUpdate(userDto, user);
    verify(userRepository, times(1)).save(user);
    verify(authService, never()).revokeAllTokensOfUser(anyLong());
    verify(userMapper, times(1)).toDto(user);
  }

  @Test
  void canUpdateUserWithRoleAndSameUsernameAndSameEmail() {
    // given
    Long userId = 1L;
    RoleDto roleDto = RoleDto.builder().id(2L).build();
    UserDto userDto =
        UserDto.builder()
            .name(faker.name().firstName())
            .surname(faker.name().lastName())
            .email(faker.internet().emailAddress())
            .username(faker.name().username())
            .role(roleDto)
            .build();
    Role role = Role.builder().id(1L).name("user").build();
    Role adminRole = Role.builder().id(2L).name("admin").build();
    User user =
        User.builder()
            .id(userId)
            .username(userDto.getUsername())
            .email(userDto.getEmail())
            .name(userDto.getName())
            .surname(userDto.getSurname())
            .password(faker.internet().password())
            .roleId(role.getId())
            .role(role)
            .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(roleRepository.findById(userDto.getRole().getId())).willReturn(Optional.of(adminRole));
    given(userRepository.existsByUsername(userDto.getUsername())).willReturn(true);
    given(userRepository.existsByEmail(userDto.getEmail())).willReturn(true);
    given(userMapper.partialUpdate(eq(userDto), any(User.class))).willReturn(user);
    given(userRepository.save(user)).willReturn(user);

    // when
    userService.updateUser(userId, userDto);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // then
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
    verify(roleRepository, times(1)).findById(userDto.getRole().getId());
    verify(userMapper, times(1)).partialUpdate(eq(userDto), userCaptor.capture());
    verify(userRepository, times(1)).save(user);
    verify(authService, never()).revokeAllTokensOfUser(anyLong());
    verify(userMapper, times(1)).toDto(user);
    assertEquals(adminRole, userCaptor.getValue().getRole());
  }

  @Test
  void canUpdateUserWithRoleWhenRoleIdIsNullAndSameUsernameAndSameEmail() {
    // given
    Long userId = 1L;
    RoleDto roleDto = RoleDto.builder().build();
    UserDto userDto =
        UserDto.builder()
            .name(faker.name().firstName())
            .surname(faker.name().lastName())
            .username(faker.name().username())
            .email(faker.internet().emailAddress())
            .role(roleDto)
            .build();
    Role role = Role.builder().id(1L).name("user").build();
    User user =
        User.builder()
            .id(userId)
            .username(userDto.getUsername())
            .email(userDto.getEmail())
            .name(userDto.getName())
            .surname(userDto.getSurname())
            .password(faker.internet().password())
            .roleId(role.getId())
            .role(role)
            .build();

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByUsername(userDto.getUsername())).willReturn(false);
    given(userRepository.existsByEmail(userDto.getEmail())).willReturn(false);
    given(userMapper.partialUpdate(userDto, user)).willReturn(user);
    given(userRepository.save(user)).willReturn(user);

    // when
    userService.updateUser(userId, userDto);

    // then
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
    verify(roleRepository, never()).findById(anyLong());
    verify(userMapper, times(1)).partialUpdate(userDto, user);
    verify(userRepository, times(1)).save(user);
    verify(authService, never()).revokeAllTokensOfUser(anyLong());
    verify(userMapper, times(1)).toDto(user);
  }

  @Test
  void cantUpdateUserWithDifferentAndExistingUsername() {
    // given
    Long userId = 1L;
    UserDto userDto = UserDto.builder().username(faker.name().username()).build();
    User user = User.builder().username(faker.name().username()).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByUsername(userDto.getUsername())).willReturn(true);

    // when
    // then
    assertThrows(
        BadRequestException.class,
        () -> userService.updateUser(userId, userDto),
        UserMessage.USERNAME_ALREADY_EXISTS);
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, never()).existsByEmail(anyString());
    verify(roleRepository, never()).findById(anyLong());
    verify(userMapper, never()).partialUpdate(any(UserDto.class), any(User.class));
    verify(userRepository, never()).save(any(User.class));
    verify(authService, never()).revokeAllTokensOfUser(anyLong());
    verify(userMapper, never()).toDto(any(User.class));
  }

  @Test
  void canUpdateUserWithDifferentUsernameAndNotExistingUsername() {
    // given
    Long userId = 1L;
    UserDto userDto = UserDto.builder().username(faker.name().username()).build();
    User user = User.builder().id(1L).username(faker.name().username()).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByUsername(userDto.getUsername())).willReturn(false);
    given(userMapper.partialUpdate(userDto, user)).willReturn(user);
    given(userRepository.save(user)).willReturn(user);

    // when
    userService.updateUser(userId, userDto);

    // then
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).existsByUsername(userDto.getUsername());
    verify(userRepository, never()).existsByEmail(anyString());
    verify(roleRepository, never()).findById(anyLong());
    verify(userMapper, times(1)).partialUpdate(userDto, user);
    verify(userRepository, times(1)).save(user);
    verify(authService, times(1)).revokeAllTokensOfUser(anyLong());
    verify(userMapper, times(1)).toDto(user);
  }

  @Test
  void cantUpdateUserWithDifferentAndExistingEmail() {
    // given
    Long userId = 1L;
    UserDto userDto = UserDto.builder().email(faker.internet().emailAddress()).build();
    User user = User.builder().email(faker.internet().emailAddress()).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByEmail(userDto.getEmail())).willReturn(true);

    // when
    // then
    assertThrows(
        BadRequestException.class,
        () -> userService.updateUser(userId, userDto),
        UserMessage.EMAIL_ALREADY_EXISTS);
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).existsByUsername(anyString());
    verify(userRepository, times(1)).existsByEmail(userDto.getEmail());
    verify(roleRepository, never()).findById(anyLong());
    verify(userMapper, never()).partialUpdate(any(UserDto.class), any(User.class));
    verify(userRepository, never()).save(any(User.class));
    verify(authService, never()).revokeAllTokensOfUser(anyLong());
    verify(userMapper, never()).toDto(any(User.class));
  }

  @Test
  void canChangePassword() {
    // given
    Long userId = 1L;
    UserLogged userLogged = UserLogged.builder().id(userId).build();
    String oldPassword = faker.internet().password();
    String newPassword = faker.internet().password();
    User user = User.builder().id(userId).password(oldPassword).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(passwordEncoder.matches(oldPassword, user.getPassword())).willReturn(true);
    given(passwordEncoder.encode(newPassword)).willReturn(newPassword);

    // when
    userService.changePassword(userLogged, oldPassword, newPassword);

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    // then
    verify(userRepository, times(1)).findById(userId);
    verify(passwordEncoder, times(1)).encode(newPassword);
    verify(userRepository, times(1)).save(userCaptor.capture());
    verify(authService, times(1)).revokeAllTokensOfUser(userId);
    assertEquals(newPassword, userCaptor.getValue().getPassword());
  }

  @Test
  void cantChangePasswordWithWrongOldPassword() {
    // given
    Long userId = 1L;
    UserLogged userLogged = UserLogged.builder().id(userId).build();
    String oldPassword = faker.internet().password();
    String newPassword = faker.internet().password();
    User user = User.builder().id(userId).password(faker.internet().password()).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(passwordEncoder.matches(oldPassword, user.getPassword())).willReturn(false);

    // when
    // then
    assertThrows(
        BadRequestException.class,
        () -> userService.changePassword(userLogged, oldPassword, newPassword),
        UserMessage.INVALID_OLD_PASSWORD);
    verify(userRepository, times(1)).findById(userId);
    verify(passwordEncoder, never()).encode(newPassword);
    verify(userRepository, never()).save(any(User.class));
    verify(authService, never()).revokeAllTokensOfUser(userId);
  }

  @Test
  void canDeleteUser() {
    // given
    Long userId = 1L;
    User user = User.builder().id(userId).build();
    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    // when
    userService.deleteUser(userId);

    // then
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).delete(user);
  }
}
