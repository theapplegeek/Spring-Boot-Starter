package it.theapplegeek.spring_starter_pack.role.controller;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import lombok.SneakyThrows;
import org.assertj.core.api.InstanceOfAssertFactories;
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
class RoleControllerTest {
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
      authorities = {"ROLE_READ"})
  void shouldGetAllRoles() {
    assertThat(mvc.get().uri("/api/role"))
        .hasStatusOk()
        .bodyJson()
        .convertTo(InstanceOfAssertFactories.list(RoleDto.class))
        .satisfies(
            roles -> {
              assertThat(roles.size()).isNotZero();
              roles.forEach(
                  (role) -> {
                    assertThat(role.getId()).isNotNull();
                    assertThat(role.getName()).isNotNull();
                    assertThat(role.getPermissions()).isNotNull();
                    assertThat(role.getPermissions()).isNotEmpty();
                  });
            });
  }
}
