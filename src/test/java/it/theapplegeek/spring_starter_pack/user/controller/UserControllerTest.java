package it.theapplegeek.spring_starter_pack.user.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import it.theapplegeek.spring_starter_pack.common.payload.JwtResponse;
import it.theapplegeek.spring_starter_pack.common.payload.LoginRequest;
import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.mapper.UserMapper;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

@Log
@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc()
class UserControllerTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine")
          .withUsername("myuser")
          .withPassword("Password1!")
          .withInitScript("db/data.sql");

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;
  @Autowired UserRepository userRepository;
  @Autowired UserMapper userMapper;
  private String adminAccessToken;
  final Faker faker = new Faker();

  @SneakyThrows
  private String loginAdmin() {
    if (adminAccessToken != null) return adminAccessToken;

    LoginRequest loginRequest = new LoginRequest("admin", "Password");

    MvcResult loginResult =
        mvc.perform(
                post("/api/auth/login")
                    .content(mapper.writeValueAsString(loginRequest))
                    .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

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
        .role(RoleDto.builder().id(faker.number().numberBetween(1L, 2L)).build())
        .enabled(true)
        .build();
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldGetAllUsers() {
    UserDto admin = userMapper.toDto(userRepository.findByUsername("admin").orElseThrow());
    UserDto user = userMapper.toDto(userRepository.findByUsername("user").orElseThrow());

    mvc.perform(
            post("/api/user/list")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "username")
                .param("direction", "asc"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(2))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.perPage").value(10))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data[0].id").value(admin.getId()))
        .andExpect(jsonPath("$.data[0].username").value(admin.getUsername()))
        .andExpect(jsonPath("$.data[0].name").value(admin.getName()))
        .andExpect(jsonPath("$.data[0].surname").value(admin.getSurname()))
        .andExpect(jsonPath("$.data[0].email").value(admin.getEmail()))
        .andExpect(jsonPath("$.data[0].role.id").value(admin.getRole().getId()))
        .andExpect(jsonPath("$.data[0].role.name").value(admin.getRole().getName()))
        .andExpect(jsonPath("$.data[1].id").value(user.getId()))
        .andExpect(jsonPath("$.data[1].username").value(user.getUsername()))
        .andExpect(jsonPath("$.data[1].name").value(user.getName()))
        .andExpect(jsonPath("$.data[1].surname").value(user.getSurname()))
        .andExpect(jsonPath("$.data[1].email").value(user.getEmail()))
        .andExpect(jsonPath("$.data[1].role.id").value(user.getRole().getId()))
        .andExpect(jsonPath("$.data[1].role.name").value(user.getRole().getName()));
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldGetAdminUserWithFilter() {
    UserDto admin = userMapper.toDto(userRepository.findByUsername("admin").orElseThrow());
    UserDto filter =
        UserDto.builder()
            .username("admin")
            .email(admin.getEmail())
            .name(admin.getName())
            .surname(admin.getSurname())
            .role(admin.getRole())
            .enabled(true)
            .build();

    mvc.perform(
            post("/api/user/list")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "username")
                .param("direction", "asc")
                .content(mapper.writeValueAsString(filter))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(1))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.perPage").value(10))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isNotEmpty())
        .andExpect(jsonPath("$.data[0].id").value(admin.getId()))
        .andExpect(jsonPath("$.data[0].username").value(admin.getUsername()))
        .andExpect(jsonPath("$.data[0].name").value(admin.getName()))
        .andExpect(jsonPath("$.data[0].surname").value(admin.getSurname()))
        .andExpect(jsonPath("$.data[0].email").value(admin.getEmail()))
        .andExpect(jsonPath("$.data[0].role.id").value(admin.getRole().getId()))
        .andExpect(jsonPath("$.data[0].role.name").value(admin.getRole().getName()));
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotGetUserWithWrongFilter() {
    UserDto filter = UserDto.builder().username("wrong").build();

    mvc.perform(
            post("/api/user/list")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "username")
                .param("direction", "asc")
                .content(mapper.writeValueAsString(filter))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(0))
        .andExpect(jsonPath("$.totalPages").value(0))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.perPage").value(10))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data").isEmpty());
  }

  @Test
  @Transactional
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldAddUser() {
    UserDto userDto = generateUserDto();

    mvc.perform(
            post("/api/user")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.username").value(userDto.getUsername()))
        .andExpect(jsonPath("$.name").value(userDto.getName()))
        .andExpect(jsonPath("$.surname").value(userDto.getSurname()))
        .andExpect(jsonPath("$.enabled").value(userDto.getEnabled()))
        .andExpect(jsonPath("$.email").value(userDto.getEmail()))
        .andExpect(jsonPath("$.role.id").value(userDto.getRole().getId()));
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotAddUserWithWrongData() {
    UserDto userDto = UserDto.builder().build();

    mvc.perform(
            post("/api/user")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotAddUserWithWrongEmail() {
    UserDto userDto = generateUserDto();
    userDto.setEmail("wrongFormat");

    mvc.perform(
            post("/api/user")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotAddUserWithWrongRole() {
    UserDto userDto = generateUserDto();
    userDto.setRole(RoleDto.builder().id(3L).name("wrong").build());

    mvc.perform(
            post("/api/user")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotAddUserWithExistingUsername() {
    UserDto userDto = generateUserDto();
    userDto.setUsername("admin");

    mvc.perform(
            post("/api/user")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotAddUserWithExistingEmail() {
    UserDto userDto = generateUserDto();
    userDto.setEmail("admin@mail.com");

    mvc.perform(
            post("/api/user")
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldUpdateUser() {
    User admin = userRepository.findByUsername("admin").orElseThrow();
    UserDto adminDto =
        UserDto.builder()
            .username("newAdmin")
            .name("newName")
            .surname("newSurname")
            .email("newAdmin@mail.com")
            .password("newPassword123!")
            .role(RoleDto.builder().id(2L).build())
            .enabled(false)
            .build();

    mvc.perform(
            put("/api/user/{id}", admin.getId())
                .content(mapper.writeValueAsString(adminDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(admin.getId()))
        .andExpect(jsonPath("$.username").value(adminDto.getUsername()))
        .andExpect(jsonPath("$.name").value(adminDto.getName()))
        .andExpect(jsonPath("$.surname").value(adminDto.getSurname()))
        .andExpect(jsonPath("$.enabled").value(adminDto.getEnabled()))
        .andExpect(jsonPath("$.email").value(adminDto.getEmail()))
        .andExpect(jsonPath("$.role.id").value(adminDto.getRole().getId()));

    User updated = userRepository.findByUsername("newAdmin").orElseThrow();
    assertEquals(updated.getPassword(), admin.getPassword());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotUpdateUserWithoutData() {
    User admin = userRepository.findByUsername("admin").orElseThrow();

    mvc.perform(put("/api/user/{id}", admin.getId()).contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotUpdateUserWithNotExistingUser() {
    UserDto adminDto = generateUserDto();

    mvc.perform(
            put("/api/user/{id}", 1000)
                .content(mapper.writeValueAsString(adminDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotUpdateUserWithExistingUsername() {
    User admin = userRepository.findByUsername("admin").orElseThrow();
    UserDto adminDto = generateUserDto();
    adminDto.setUsername("user");

    mvc.perform(
            put("/api/user/{id}", admin.getId())
                .content(mapper.writeValueAsString(adminDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotUpdateUserWithExistingEmail() {
    User admin = userRepository.findByUsername("admin").orElseThrow();
    UserDto adminDto = generateUserDto();
    adminDto.setEmail("user@mail.com");

    mvc.perform(
            put("/api/user/{id}", admin.getId())
                .content(mapper.writeValueAsString(adminDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotUpdateUserWithWrongRole() {
    User admin = userRepository.findByUsername("admin").orElseThrow();
    UserDto adminDto = generateUserDto();
    adminDto.setRole(RoleDto.builder().id(3L).name("wrong").build());

    mvc.perform(
            put("/api/user/{id}", admin.getId())
                .content(mapper.writeValueAsString(adminDto))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isNotFound());
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldChangePassword() {
    String token = loginAdmin();
    User admin = userRepository.findByUsername("admin").orElseThrow();

    Map<String, String> data = Map.of("oldPassword", "Password", "newPassword", "newPassword123!");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    mvc.perform(
            put("/api/user/change-password")
                .headers(headers)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

    User updated = userRepository.findByUsername("admin").orElseThrow();
    assertNotEquals(updated.getPassword(), admin.getPassword());
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldNotChangePasswordWithWrongPassword() {
    String token = loginAdmin();

    Map<String, String> data =
        Map.of("oldPassword", "WrongPassword", "newPassword", "newPassword123!");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    mvc.perform(
            put("/api/user/change-password")
                .headers(headers)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldNotChangePasswordWithoutNewPassword() {
    String token = loginAdmin();

    Map<String, String> data = Map.of("oldPassword", "Password");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    mvc.perform(
            put("/api/user/change-password")
                .headers(headers)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  @SneakyThrows
  void shouldNotChangePasswordWithoutPassingOldPassword() {
    String token = loginAdmin();

    Map<String, String> data = Map.of("newPassowrd", "Password123");
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Bearer " + token);

    mvc.perform(
            put("/api/user/change-password")
                .headers(headers)
                .content(mapper.writeValueAsString(data))
                .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldDeleteUser() {
    User user = userRepository.findByUsername("user").orElseThrow();

    mvc.perform(delete("/api/user/{id}", user.getId())).andDo(print()).andExpect(status().isOk());

    assertTrue(userRepository.findByUsername("user").isEmpty());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"ROLE_ADMIN"})
  void shouldNotDeleteNotExistingUser() {
    mvc.perform(delete("/api/user/{id}", 1000)).andDo(print()).andExpect(status().isNotFound());
  }
}
