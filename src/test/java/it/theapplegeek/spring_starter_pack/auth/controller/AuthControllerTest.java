package it.theapplegeek.spring_starter_pack.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.theapplegeek.spring_starter_pack.common.payload.JwtResponse;
import it.theapplegeek.spring_starter_pack.common.payload.LoginRequest;
import it.theapplegeek.spring_starter_pack.security.service.JwtService;
import it.theapplegeek.spring_starter_pack.token.model.Token;
import it.theapplegeek.spring_starter_pack.token.model.TokenType;
import it.theapplegeek.spring_starter_pack.token.repository.TokenRepository;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
class AuthControllerTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine")
          .withUsername("myuser")
          .withPassword("Password1!")
          .withInitScript("db/data.sql");

  @Container
  static RabbitMQContainer rabbitMQContainer =
      new RabbitMQContainer("rabbitmq:3.13.2-management-alpine")
          .withEnv("RABBITMQ_DEFAULT_USER", "rabbitmq")
          .withEnv("RABBITMQ_DEFAULT_PASS", "Password1!")
          .withAdminPassword("Password1!");

  @Autowired private MockMvcTester mvc;
  @Autowired private ObjectMapper mapper;
  @Autowired private JwtService jwtService;
  @Autowired private TokenRepository tokenRepository;
  @Autowired private UserRepository userRepository;

  @BeforeAll
  static void setUp() {
    System.setProperty("spring.rabbitmq.host", rabbitMQContainer.getHost());
    System.setProperty("spring.rabbitmq.port", rabbitMQContainer.getAmqpPort().toString());
  }

  @SneakyThrows
  private JwtResponse loginAdmin() {
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
        .satisfies(
            jwtResponse -> {
              assertThat(jwtResponse.getAccessToken()).isNotNull();
              assertThat(jwtResponse.getRefreshToken()).isNotNull();
            });

    return mapper.readValue(loginResult.getResponse().getContentAsString(), JwtResponse.class);
  }

  @SneakyThrows
  private String getResetPasswordToken() {
    Map<String, String> body = new HashMap<>();
    body.put("email", "user@mail.com");

    assertThat(
            mvc.post()
                .uri("/api/auth/forgot-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatusOk();

    return tokenRepository
        .findAllByTokenTypeAndUserEmailAndRevokedIsFalse(TokenType.RESET_PASSWORD, "user@mail.com")
        .getFirst()
        .getToken();
  }

  @Test
  @SneakyThrows
  void shouldAuthenticateUser() {
    // Given
    LoginRequest loginRequest = new LoginRequest("admin", "Password");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
        .hasStatusOk()
        .bodyJson()
        .convertTo(JwtResponse.class)
        .satisfies(
            jwtResponse -> {
              assertThat(jwtResponse.getAccessToken()).isNotNull();
              assertThat(jwtResponse.getRefreshToken()).isNotNull();
              assertThat(jwtService.extractUsername(jwtResponse.getAccessToken()))
                  .isEqualTo("admin");
            });
  }

  @Test
  @SneakyThrows
  void shouldNotAuthenticateWithInvalidCredentials() {
    // Given
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("username", "invalidUser");
    loginRequest.put("password", "wrongPassword");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @SneakyThrows
  void shouldNotAuthenticateWithoutPasswordBodyField() {
    // Given
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("username", "invalidUser");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  void shouldNotAuthenticateWithoutUsernameBodyField() {
    // Given
    Map<String, String> loginRequest = new HashMap<>();
    loginRequest.put("password", "wrongPassword");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/login")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(loginRequest)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldRefreshToken() {
    // Given
    JwtResponse jwtResponse = loginAdmin();
    String refreshToken = "Bearer " + jwtResponse.getRefreshToken();

    // When
    assertThat(mvc.post().uri("/api/auth/refresh-token").header("Authorization", refreshToken))
        .hasStatusOk()
        .bodyJson()
        .convertTo(JwtResponse.class)
        .satisfies(
            response -> {
              assertThat(response.getAccessToken()).isNotNull();
              assertThat(response.getRefreshToken()).isNotNull();
              assertThat(jwtService.extractUsername(response.getAccessToken())).isEqualTo("admin");
            });
  }

  @Test
  void shouldNotRefreshTokenWithInvalidRefreshToken() {
    // Given
    String invalidRefreshToken = "Bearer invalid-refresh-token";

    // When
    // Then
    assertThat(
            mvc.post().uri("/api/auth/refresh-token").header("Authorization", invalidRefreshToken))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldNotRefreshTokenWithoutRefreshToken() {
    assertThat(mvc.post().uri("/api/auth/refresh-token")).hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  void shouldSendForgotPasswordEmail() {
    // Given
    Map<String, String> body = new HashMap<>();
    body.put("email", "admin@mail.com");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/forgot-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatusOk();
  }

  @Test
  @SneakyThrows
  void shouldNotSendForgotPasswordForInvalidEmail() {
    // Given
    Map<String, String> body = new HashMap<>();
    body.put("email", "nonexistent@example.com");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/forgot-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatusOk();
  }

  @Test
  @SneakyThrows
  void shouldNotSendForgotPasswordForInvalidBody() {
    // Given
    Map<String, String> body = new HashMap<>();

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/forgot-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  void shouldResetPassword() {
    // Given
    User user = userRepository.findByUsername("user").orElseThrow();
    String oldPassword = user.getPassword();
    String resetPasswordToken = getResetPasswordToken();
    Map<String, String> body = new HashMap<>();
    body.put("token", resetPasswordToken);
    body.put("newPassword", "NewSecurePassword1!");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/reset-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatusOk();

    User updated = userRepository.findByUsername("user").orElseThrow();
    assertNotEquals(updated.getPassword(), oldPassword);
  }

  @Test
  @SneakyThrows
  void shouldNotResetPasswordForInvalidToken() {
    // Given
    Map<String, String> body = new HashMap<>();
    body.put("token", "invalid-reset-token");
    body.put("newPassword", "NewSecurePassword1!");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/reset-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatus(HttpStatus.UNAUTHORIZED);
  }

  @Test
  @SneakyThrows
  void shouldNotResetPasswordWithoutNewPasswordBodyField() {
    // Given
    Map<String, String> body = new HashMap<>();
    body.put("token", "invalid-reset-token");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/reset-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  @SneakyThrows
  void shouldNotResetPasswordWithoutTokenBodyField() {
    // Given
    Map<String, String> body = new HashMap<>();
    body.put("newPassword", "NewSecurePassword1!");

    // When
    // Then
    assertThat(
            mvc.post()
                .uri("/api/auth/reset-password")
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(body)))
        .hasStatus(HttpStatus.BAD_REQUEST);
  }

  @Test
  void shouldLogout() {
    // Given
    JwtResponse jwtResponse = loginAdmin();
    String token = "Bearer " + jwtResponse.getAccessToken();

    // When
    // Then
    assertThat(mvc.post().uri("/api/auth/logout").header("Authorization", token)).hasStatusOk();

    Optional<Token> revokedToken = tokenRepository.findByToken(jwtResponse.getAccessToken());
    assertTrue(revokedToken.isPresent());
    assertTrue(revokedToken.get().getRevoked());
  }

  @Test
  void shouldNotLogoutWithInvalidToken() {
    // Given
    String invalidToken = "Bearer invalid-access-token";

    // When
    // Then
    assertThat(mvc.post().uri("/api/auth/logout").header("Authorization", invalidToken))
        .hasStatusOk();
  }

  @Test
  void shouldNotLogoutWithoutToken() {
    assertThat(mvc.post().uri("/api/auth/logout")).hasStatus(HttpStatus.BAD_REQUEST);
  }
}
