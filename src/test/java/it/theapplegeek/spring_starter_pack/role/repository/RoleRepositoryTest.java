package it.theapplegeek.spring_starter_pack.role.repository;

import static org.junit.jupiter.api.Assertions.*;

import it.theapplegeek.spring_starter_pack.role.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine")
          .withInitScript("db/data.sql");

  @Autowired RoleRepository roleRepository;

  @Test
  void shouldFindRoleByName() {
    Role admin = roleRepository.findByName("admin").orElseThrow();
    assertEquals("admin", admin.getName());
  }

  @Test
  void shouldNotFindRoleByName() {
    assertFalse(roleRepository.findByName("superuser").isPresent());
  }
}
