package it.theapplegeek.spring_starter_pack.user.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;
import it.theapplegeek.spring_starter_pack.common.util.pagination.PagedRequestParams;
import it.theapplegeek.spring_starter_pack.role.dto.RoleDto;
import it.theapplegeek.spring_starter_pack.user.dto.UserDto;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.model.UserRole;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase
class UserRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine").withInitScript("db/data.sql");

  @Autowired UserRepository userRepository;
  @Autowired UserRoleRepository userRoleRepository;
  final Faker faker = new Faker();

  private void generateAndSaveUser() {
    User user =
        userRepository.save(
            User.builder()
                .username(faker.name().username())
                .name(faker.name().firstName())
                .surname(faker.name().lastName())
                .email(faker.internet().emailAddress())
                .password(faker.internet().password())
                .userRoles(new ArrayList<>())
                .build());

    UserRole userRole =
        userRoleRepository.save(
            UserRole.builder()
                .id(
                    UserRole.UserRolePK.builder()
                        .userId(user.getId())
                        .roleId(faker.number().numberBetween(1L, 2L))
                        .build())
                .build());

    user.getUserRoles().add(userRole);
  }

  @Test
  void shouldExistByUsername() {
    assertTrue(userRepository.existsByUsername("user"));
  }

  @Test
  void shouldNotExistByUsername() {
    assertFalse(userRepository.existsByUsername("unknown"));
  }

  @Test
  void shouldExistByEmail() {
    assertTrue(userRepository.existsByEmail("admin@mail.com"));
  }

  @Test
  void shouldNotExistByEmail() {
    assertFalse(userRepository.existsByEmail("unknow@mail.com"));
  }

  @Test
  void shouldFindAllWithSpecificationAndPageable() {
    // given
    UserDto search =
        UserDto.builder().username("adm").email("admin@").name("Admin").surname("Ad").build();
    Pageable pageable =
        PagedRequestParams.builder()
            .page(0)
            .size(10)
            .sort("username")
            .direction("asc")
            .build()
            .asPageable();

    // when
    Page<User> users = userRepository.findAll(search, pageable);

    // then
    assertEquals(1, users.getTotalElements());
    assertEquals("Admin", users.getContent().getFirst().getName());
    assertEquals("admin", users.getContent().getFirst().getUsername());
    assertEquals("admin@mail.com", users.getContent().getFirst().getEmail());
  }

  @Test
  void shouldFindAllByRoleWithSpecificationAndPageable() {
    // given
    UserDto search = UserDto.builder().roles(List.of(RoleDto.builder().id(2L).build())).build();
    Pageable pageable =
        PagedRequestParams.builder()
            .page(0)
            .size(10)
            .sort("username")
            .direction("asc")
            .build()
            .asPageable();

    // when
    Page<User> users = userRepository.findAll(search, pageable);

    // then
    assertEquals(1, users.getTotalElements());
    assertEquals("Admin", users.getContent().getFirst().getName());
    assertEquals("admin", users.getContent().getFirst().getUsername());
    assertEquals("admin@mail.com", users.getContent().getFirst().getEmail());
  }

  @Test
  @Rollback
  void shouldFindAllWithPageable() {
    // given
    generateAndSaveUser();
    generateAndSaveUser();

    Pageable pageable =
        PagedRequestParams.builder()
            .page(1)
            .size(2)
            .sort("username")
            .direction("asc")
            .build()
            .asPageable();

    // when
    Page<User> users = userRepository.findAll(pageable);

    // then
    assertEquals(4, users.getTotalElements());
    assertEquals(2, users.getTotalPages());
    assertEquals(2, users.getSize());
    assertEquals(2, users.getContent().size());
  }

  @Test
  void shouldFindByUsername() {
    // given
    // when
    Optional<User> user = userRepository.findByUsername("user");

    // then
    assertTrue(user.isPresent());
    assertEquals("User", user.get().getName());
    assertEquals("user", user.get().getUsername());
    assertEquals("user@mail.com", user.get().getEmail());
  }

  @Test
  void shouldNotFindByUsername() {
    assertTrue(userRepository.findByUsername("unknown").isEmpty());
  }

  @Test
  void shouldFindByEmailAndEnabledIsTrue() {
    // given
    // when
    Optional<User> admin = userRepository.findByEmailAndEnabledIsTrue("admin@mail.com");

    // then
    assertTrue(admin.isPresent());
    assertEquals("Admin", admin.get().getName());
    assertEquals("admin", admin.get().getUsername());
    assertEquals("admin@mail.com", admin.get().getEmail());
  }

  @Test
  void shouldNotFindByEmailAndEnabledIsTrue() {
    assertTrue(userRepository.findByEmailAndEnabledIsTrue("unknown@mail.com").isEmpty());
  }

  @Test
  @Rollback
  void shouldNotFindByEmailAndEnabledIsTrueWhenDisabled() {
    // given
    User user = userRepository.findByUsername("user").orElseThrow();
    user.setEnabled(false);
    userRepository.save(user);

    // when
    // then
    assertTrue(userRepository.findByEmailAndEnabledIsTrue("user@mail.com").isEmpty());
  }
}
