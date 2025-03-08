package it.theapplegeek.spring_starter_pack.permission.controller;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.util.List;
import lombok.SneakyThrows;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class PermissionControllerTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine")
          .withUsername("myuser")
          .withPassword("Password1!")
          .withInitScript("db/data.sql");

  @Autowired MockMvcTester mvc;

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"PERMISSION_READ"})
  void shouldGetAllRoles() {
    assertThat(mvc.get().uri("/api/permission"))
        .hasStatusOk()
        .bodyJson()
        .convertTo(List.class)
        .satisfies(permission -> AssertionsForClassTypes.assertThat(permission.size()).isNotZero());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"PERMISSION_READ"})
  void shouldGetAllPermissionsByRoleId() {
    assertThat(mvc.get().uri("/api/permission/role/1"))
        .hasStatusOk()
        .bodyJson()
        .convertTo(List.class)
        .satisfies(permission -> assertThat(permission.size()).isNotZero());
  }

  @Test
  @SneakyThrows
  @WithMockUser(
      username = "admin",
      authorities = {"PERMISSION_READ"})
  void shouldNotGetAllPermissionsByRoleId() {
    assertThat(mvc.get().uri("/api/permission/role/1000"))
        .hasStatusOk()
        .bodyJson()
        .convertTo(List.class)
        .satisfies(permission -> assertThat(permission.size()).isZero());
  }
}
