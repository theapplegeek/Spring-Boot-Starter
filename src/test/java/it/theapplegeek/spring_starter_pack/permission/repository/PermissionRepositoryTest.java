package it.theapplegeek.spring_starter_pack.permission.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.role.repository.RoleRepository;
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
@AutoConfigureTestDatabase
class PermissionRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine").withInitScript("db/data.sql");

  @Autowired PermissionRepository permissionRepository;
  @Autowired RoleRepository roleRepository;

  @Test
  void shouldFindPermissionByRoleId() {
    Role role = roleRepository.findByName("admin").orElseThrow();
    assertThat(permissionRepository.findAllByRolePermissions_Id_RoleId(role.getId()).size())
        .isNotZero();
  }

  @Test
  void shouldNotFindPermissionByRoleId() {
    assertThat(permissionRepository.findAllByRolePermissions_Id_RoleId(1000L).size()).isZero();
  }
}
