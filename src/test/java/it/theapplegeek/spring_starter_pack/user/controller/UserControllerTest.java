package it.theapplegeek.spring_starter_pack.user.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.mapper.UserMapper;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
}
