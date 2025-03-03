package it.theapplegeek.spring_starter_pack.token.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.github.javafaker.Faker;
import it.theapplegeek.spring_starter_pack.token.model.Token;
import it.theapplegeek.spring_starter_pack.token.model.TokenType;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.Rollback;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TokenRepositoryTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine").withInitScript("db/data.sql");

  @Autowired TokenRepository tokenRepository;
  @Autowired UserRepository userRepository;
  Faker faker = new Faker();

  void insertJwtTokenForUser(User user, String jwtToken) {
    Token token =
        Token.builder()
            .userId(user.getId())
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .revoked(false)
            .expiration(LocalDateTime.now().plusDays(1))
            .build();
    tokenRepository.save(token);
  }

  @Test
  @Rollback
  void shouldFindTokenByToken() {
    // given
    User user = userRepository.findByUsername("admin").orElseThrow();
    String jwtToken = faker.internet().uuid();
    insertJwtTokenForUser(user, jwtToken);

    // when
    // then
    assertTrue(tokenRepository.findByToken(jwtToken).isPresent());
    assertEquals(jwtToken, tokenRepository.findByToken(jwtToken).get().getToken());
  }

  @Test
  void shouldNotFindTokenByToken() {
    assertFalse(tokenRepository.findByToken("invalid").isPresent());
  }

  @Test
  @Rollback
  void shouldDeleteAllByExpirationDate() {
    // given
    User user = userRepository.findByUsername("admin").orElseThrow();
    String jwtToken = faker.internet().uuid();
    String jwtToken2 = faker.internet().uuid();
    insertJwtTokenForUser(user, jwtToken);
    insertJwtTokenForUser(user, jwtToken2);

    // when
    tokenRepository.deleteAllByExpirationDate(LocalDateTime.now().plusMonths(1));

    // then
    assertTrue(tokenRepository.findAll().isEmpty());
  }

  @Test
  @Rollback
  void shouldRevokeAllByUserIdAndType() {
    // given
    User user = userRepository.findByUsername("admin").orElseThrow();
    String jwtToken = faker.internet().uuid();
    String jwtToken2 = faker.internet().uuid();
    insertJwtTokenForUser(user, jwtToken);
    insertJwtTokenForUser(user, jwtToken2);

    // when
    tokenRepository.revokeAllByUserIdAndType(user.getId(), TokenType.BEARER);

    // then
    assertTrue(tokenRepository.findAll().stream().allMatch(Token::getRevoked));
  }

  @Test
  @Rollback
  void shouldFindTokenByTokenAndTokenTypeAndRevokedIsFalse() {
    // given
    User user = userRepository.findByUsername("admin").orElseThrow();
    String jwtToken = faker.internet().uuid();
    insertJwtTokenForUser(user, jwtToken);

    // when
    Optional<Token> token =
        tokenRepository.findByTokenAndTokenTypeAndRevokedIsFalse(jwtToken, TokenType.BEARER);

    // then
    assertTrue(token.isPresent());
    assertEquals(jwtToken, token.get().getToken());
  }

  @Test
  void shouldNotFindTokenByTokenAndTokenTypeAndRevokedIsFalse() {
    assertFalse(
        tokenRepository
            .findByTokenAndTokenTypeAndRevokedIsFalse("invalid", TokenType.BEARER)
            .isPresent());
  }

  @Test
  @Rollback
  void shouldNotFindTokenByTokenAndTokenTypeAndRevokedIsTrue() {
    // given
    User user = userRepository.findByUsername("admin").orElseThrow();
    String jwtToken = faker.internet().uuid();
    insertJwtTokenForUser(user, jwtToken);

    // when
    tokenRepository.revokeAllByUserIdAndType(user.getId(), TokenType.BEARER);

    // then
    assertFalse(
        tokenRepository
            .findByTokenAndTokenTypeAndRevokedIsFalse(jwtToken, TokenType.BEARER)
            .isPresent());
  }

  @Test
  @Rollback
  void shouldNotFindTokenByTokenAndTokenTypeAndRevokedIsFalseWithDifferentTokenType() {
    // given
    User user = userRepository.findByUsername("admin").orElseThrow();
    String jwtToken = faker.internet().uuid();
    insertJwtTokenForUser(user, jwtToken);

    // when
    // then
    assertFalse(
        tokenRepository
            .findByTokenAndTokenTypeAndRevokedIsFalse(jwtToken, TokenType.RESET_PASSWORD)
            .isPresent());
  }
}
