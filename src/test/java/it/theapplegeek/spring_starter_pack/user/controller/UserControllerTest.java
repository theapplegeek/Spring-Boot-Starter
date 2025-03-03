package it.theapplegeek.spring_starter_pack.user.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import it.theapplegeek.spring_starter_pack.common.payload.JwtResponse;
import it.theapplegeek.spring_starter_pack.common.payload.LoginRequest;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedListDto;
import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.mapper.UserMapper;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class UserControllerTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine")
          .withUsername("myuser")
          .withPassword("Password1!")
          .withInitScript("db/data.sql");

  @Autowired MockMvcTester mvc;
  @Autowired ObjectMapper mapper;
  @Autowired UserRepository userRepository;
  @Autowired UserMapper userMapper;
  private String adminAccessToken;
  final Faker faker = new Faker();

  @SneakyThrows
  private String loginAdmin() {
    if (adminAccessToken != null) return adminAccessToken;

    LoginRequest loginRequest = new LoginRequest("admin", "Password");

    MvcTestResult loginResult =
        mvc.post()
            .uri("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(loginRequest))
            .exchange();

    assertThat(loginResult).hasStatusOk();
    assertThat(loginResult)
        .bodyJson()
        .convertTo(JwtResponse.class)
        .satisfies(jwtResponse -> assertThat(jwtResponse.getAccessToken()).isNotNull());

    adminAccessToken = loginResult.getResponse().getContentAsString();

    adminAccessToken =
        mapper
            .readValue(loginResult.getResponse().getContentAsString(), JwtResponse.class)
            .getAccessToken();
    return adminAccessToken;
  }

  private UserDto generateUserDto() {
    return UserDto.builder()
        .username(faker.name().username())
        .name(faker.name().firstName())
        .surname(faker.name().lastName())
        .email(faker.internet().emailAddress())
        .password(faker.internet().password())
        .roles(List.of(RoleDto.builder().id(faker.number().numberBetween(1L, 2L)).build()))
        .enabled(true)
        .build();
  }

  @Test
  @Transactional
  @WithMockUser(
      username = "admin",
      authorities = {"USER_READ"})
  void shouldGetAllUsers() {
    // given
    UserDto admin = userMapper.toDto(userRepository.findByUsername("admin").orElseThrow());
    UserDto user = userMapper.toDto(userRepository.findByUsername("user").orElseThrow());

    // when
    MvcTestResult result =
        mvc.post()
            .uri("/api/user/list")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "username")
            .param("direction", "asc")
            .exchange();

    // then
    assertThat(result).hasStatusOk();
    assertThat(result)
        .bodyJson()
        .convertTo(PagedListDto.class)
        .satisfies(
            pagedListDto -> {
              assertThat(pagedListDto.getTotal()).isEqualTo(2);
              assertThat(pagedListDto.getTotalPages()).isEqualTo(1);
              assertThat(pagedListDto.getPage()).isEqualTo(0);
              assertThat(pagedListDto.getPerPage()).isEqualTo(10);
            });
    assertThat(result)
        .bodyJson()
        .extractingPath("$.data")
        .convertTo(InstanceOfAssertFactories.list(UserDto.class))
        .hasSize(2)
        .satisfies(
            userDtoList -> {
              assertThat(userDtoList.getFirst().getId()).isEqualTo(admin.getId());
              assertThat(userDtoList.getFirst().getUsername()).isEqualTo(admin.getUsername());
              assertThat(userDtoList.getFirst().getName()).isEqualTo(admin.getName());
              assertThat(userDtoList.getFirst().getSurname()).isEqualTo(admin.getSurname());
              assertThat(userDtoList.getFirst().getEmail()).isEqualTo(admin.getEmail());
              assertThat(userDtoList.getFirst().getRoles().size()).isEqualTo(1);
              assertThat(userDtoList.getFirst().getRoles().getFirst().getId())
                  .isEqualTo(admin.getRoles().getFirst().getId());
              assertThat(userDtoList.getFirst().getRoles().getFirst().getName())
                  .isEqualTo(admin.getRoles().getFirst().getName());
              assertThat(userDtoList.getLast().getId()).isEqualTo(user.getId());
              assertThat(userDtoList.getLast().getUsername()).isEqualTo(user.getUsername());
              assertThat(userDtoList.getLast().getName()).isEqualTo(user.getName());
              assertThat(userDtoList.getLast().getSurname()).isEqualTo(user.getSurname());
              assertThat(userDtoList.getLast().getEmail()).isEqualTo(user.getEmail());
              assertThat(userDtoList.getLast().getRoles().getFirst().getId())
                  .isEqualTo(user.getRoles().getFirst().getId());
              assertThat(userDtoList.getLast().getRoles().getFirst().getName())
                  .isEqualTo(user.getRoles().getFirst().getName());
            });
  }

  @Test
  @SneakyThrows
  @Transactional
  @WithMockUser(
      username = "admin",
      authorities = {"USER_READ"})
  void shouldGetAdminUserWithFilter() {
    // given
    UserDto admin = userMapper.toDto(userRepository.findByUsername("admin").orElseThrow());
    UserDto filter =
        UserDto.builder()
            .username("admin")
            .email(admin.getEmail())
            .name(admin.getName())
            .surname(admin.getSurname())
            .roles(List.of(RoleDto.builder().id(admin.getRoles().getFirst().getId()).build()))
            .enabled(true)
            .build();

    // when
    MvcTestResult result =
        mvc.post()
            .uri("/api/user/list")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "username")
            .param("direction", "asc")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(filter))
            .exchange();

    // then
    assertThat(result).hasStatusOk();
    assertThat(result)
        .bodyJson()
        .convertTo(PagedListDto.class)
        .satisfies(
            pagedListDto -> {
              assertThat(pagedListDto.getTotal()).isEqualTo(1);
              assertThat(pagedListDto.getTotalPages()).isEqualTo(1);
              assertThat(pagedListDto.getPage()).isEqualTo(0);
              assertThat(pagedListDto.getPerPage()).isEqualTo(10);
            });
    assertThat(result)
        .bodyJson()
        .extractingPath("$.data")
        .convertTo(InstanceOfAssertFactories.list(UserDto.class))
        .hasSize(1)
        .satisfies(
            userDtoList -> {
              assertThat(userDtoList.getFirst().getId()).isEqualTo(admin.getId());
              assertThat(userDtoList.getFirst().getUsername()).isEqualTo(admin.getUsername());
              assertThat(userDtoList.getFirst().getName()).isEqualTo(admin.getName());
              assertThat(userDtoList.getFirst().getSurname()).isEqualTo(admin.getSurname());
              assertThat(userDtoList.getFirst().getEmail()).isEqualTo(admin.getEmail());
              assertThat(userDtoList.getFirst().getRoles().getFirst().getId())
                  .isEqualTo(admin.getRoles().getFirst().getId());
              assertThat(userDtoList.getFirst().getRoles().getFirst().getName())
                  .isEqualTo(admin.getRoles().getFirst().getName());
            });
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_READ"})
  void shouldNotGetUserWithWrongFilter() {
    // given
    UserDto filter = UserDto.builder().username("wrong").build();

    // when
    MvcTestResult result =
        mvc.post()
            .uri("/api/user/list")
            .param("page", "0")
            .param("size", "10")
            .param("sort", "username")
            .param("direction", "asc")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(filter))
            .exchange();

    // then
    assertThat(result).hasStatusOk();
    assertThat(result)
        .bodyJson()
        .convertTo(PagedListDto.class)
        .satisfies(
            pagedListDto -> {
              assertThat(pagedListDto.getTotal()).isEqualTo(0);
              assertThat(pagedListDto.getTotalPages()).isEqualTo(0);
              assertThat(pagedListDto.getPage()).isEqualTo(0);
              assertThat(pagedListDto.getPerPage()).isEqualTo(10);
            });
    assertThat(result)
        .bodyJson()
        .extractingPath("$.data")
        .convertTo(InstanceOfAssertFactories.list(UserDto.class))
        .isEmpty();
  }

  @Test
  @Transactional
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_CREATE"})
  void shouldAddUser() {
    // given
    UserDto userDto = generateUserDto();

    // when
    // then
    assertThat(
            mvc.post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDto)))
        .hasStatusOk()
        .bodyJson()
        .convertTo(UserDto.class)
        .satisfies(
            user -> {
              assertThat(user.getId()).isNotNull();
              assertThat(user.getUsername()).isEqualTo(userDto.getUsername());
              assertThat(user.getName()).isEqualTo(userDto.getName());
              assertThat(user.getSurname()).isEqualTo(userDto.getSurname());
              assertThat(user.getEmail()).isEqualTo(userDto.getEmail());
              assertThat(user.getRoles().getFirst().getId())
                  .isEqualTo(userDto.getRoles().getFirst().getId());
            });
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_CREATE"})
  void shouldNotAddUserWithWrongData() {
    // given
    UserDto userDto = UserDto.builder().build();

    // when
    // then
    assertThat(
            mvc.post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDto)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_CREATE"})
  void shouldNotAddUserWithWrongEmail() {
    // given
    UserDto userDto = generateUserDto();
    userDto.setEmail("wrongFormat");

    // when
    // then
    assertThat(
            mvc.post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDto)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_CREATE"})
  void shouldNotAddUserWithWrongRole() {
    // given
    UserDto userDto = generateUserDto();
    userDto.setRoles(List.of(RoleDto.builder().id(3L).name("wrong").build()));

    // when
    // then
    assertThat(
            mvc.post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDto)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_CREATE"})
  void shouldNotAddUserWithExistingUsername() {
    // given
    UserDto userDto = generateUserDto();
    userDto.setUsername("admin");

    // when
    // then
    assertThat(
            mvc.post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDto)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_CREATE"})
  void shouldNotAddUserWithExistingEmail() {
    // given
    UserDto userDto = generateUserDto();
    userDto.setEmail("admin@mail.com");

    // when
    // then
    assertThat(
            mvc.post()
                .uri("/api/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(userDto)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @Transactional
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_UPDATE"})
  void shouldUpdateUser() {
    // given
    User admin = userRepository.findByUsername("admin").orElseThrow();
    String oldHashedPassword = admin.getPassword();
    List<RoleDto> roles = new ArrayList<>();
    roles.add(RoleDto.builder().id(2L).build());
    UserDto adminDto =
        UserDto.builder()
            .username("newAdmin")
            .name("newName")
            .surname("newSurname")
            .email("newAdmin@mail.com")
            .password("newPassword123!")
            .roles(roles)
            .enabled(false)
            .build();

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/{id}", admin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(adminDto)))
        .hasStatusOk()
        .bodyJson()
        .convertTo(UserDto.class)
        .satisfies(
            user -> {
              assertThat(user.getId()).isEqualTo(admin.getId());
              assertThat(user.getUsername()).isEqualTo(adminDto.getUsername());
              assertThat(user.getName()).isEqualTo(adminDto.getName());
              assertThat(user.getSurname()).isEqualTo(adminDto.getSurname());
              assertThat(user.getEmail()).isEqualTo(adminDto.getEmail());
              assertThat(user.getRoles().getFirst().getId())
                  .isEqualTo(adminDto.getRoles().getFirst().getId());
              assertThat(user.getEnabled()).isEqualTo(adminDto.getEnabled());
            });

    userRepository.flush();
    User updated = userRepository.findByUsername("newAdmin").orElseThrow();
    assertEquals(updated.getPassword(), oldHashedPassword);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_UPDATE"})
  void shouldNotUpdateUserWithoutData() {
    // given
    User admin = userRepository.findByUsername("admin").orElseThrow();

    // when
    // then
    assertThat(
            mvc.put().uri("/api/user/{id}", admin.getId()).contentType(MediaType.APPLICATION_JSON))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_UPDATE"})
  void shouldNotUpdateUserWithNotExistingUser() {
    // given
    UserDto adminDto = generateUserDto();

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/{id}", 1000)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(adminDto)))
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_UPDATE"})
  void shouldNotUpdateUserWithExistingUsername() {
    // given
    User admin = userRepository.findByUsername("admin").orElseThrow();
    UserDto adminDto = generateUserDto();
    adminDto.setUsername("user");

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/{id}", admin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(adminDto)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_UPDATE"})
  void shouldNotUpdateUserWithExistingEmail() {
    // given
    User admin = userRepository.findByUsername("admin").orElseThrow();
    UserDto adminDto = generateUserDto();
    adminDto.setEmail("user@mail.com");

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/{id}", admin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(adminDto)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_UPDATE"})
  void shouldNotUpdateUserWithWrongRole() {
    // given
    User admin = userRepository.findByUsername("admin").orElseThrow();
    UserDto adminDto = generateUserDto();
    adminDto.setRoles(List.of(RoleDto.builder().id(3L).name("wrong").build()));

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/{id}", admin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(adminDto)))
        .hasStatus(HttpStatus.NOT_FOUND);
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldChangePassword() {
    // given
    String token = loginAdmin();
    User admin = userRepository.findByUsername("admin").orElseThrow();
    String oldHashedPassword = admin.getPassword();

    Map<String, String> data = Map.of("oldPassword", "Password", "newPassword", "newPassword123!");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/change-password")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
        .hasStatusOk();

    User updated = userRepository.findByUsername("admin").orElseThrow();
    assertNotEquals(updated.getPassword(), oldHashedPassword);
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldNotChangePasswordWithWrongPassword() {
    // given
    String token = loginAdmin();

    Map<String, String> data =
        Map.of("oldPassword", "WrongPassword", "newPassword", "newPassword123!");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/change-password")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldNotChangePasswordWithoutNewPassword() {
    // given
    String token = loginAdmin();

    Map<String, String> data = Map.of("oldPassword", "Password");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/change-password")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldNotChangePasswordWithoutPassingOldPassword() {
    // given
    String token = loginAdmin();

    Map<String, String> data = Map.of("newPassowrd", "Password123");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    // when
    // then
    assertThat(
            mvc.put()
                .uri("/api/user/change-password")
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(data)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @Transactional
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_DELETE"})
  void shouldDeleteUser() {
    // given
    // when
    User user = userRepository.findByUsername("user").orElseThrow();

    // then
    assertThat(mvc.delete().uri("/api/user/{id}", user.getId())).hasStatusOk();
    assertTrue(userRepository.findByUsername("user").isEmpty());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"USER_DELETE"})
  void shouldNotDeleteNotExistingUser() {
    assertThat(mvc.delete().uri("/api/user/{id}", 1000)).hasStatus(HttpStatus.NOT_FOUND);
  }
}
