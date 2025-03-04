package it.theapplegeek.spring_starter_pack.security.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import it.theapplegeek.spring_starter_pack.user.model.User;
import java.lang.reflect.Method;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
  @Mock private User userDetails;
  @InjectMocks private JwtService jwtService;

  private final String secretKey = "thisIsASecretKeyForJwtTestingOnlyShouldBeLong";

  @BeforeEach
  void setUp() {
    long jwtExpiration = 3600000; // 1 hour
    long refreshExpiration = 7200000; // 2 hours

    ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
    ReflectionTestUtils.setField(jwtService, "jwtExpiration", jwtExpiration);
    ReflectionTestUtils.setField(jwtService, "refreshExpiration", refreshExpiration);
  }

  @Test
  void shouldGenerateTokenSuccessfully() {
    // given
    given(userDetails.getUsername()).willReturn("testUser");

    // when
    String token = jwtService.generateToken(userDetails);

    // then
    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void shouldExtractUsernameFromToken() {
    // given
    when(userDetails.getUsername()).thenReturn("testUser");
    String token = jwtService.generateToken(userDetails);

    // when
    String extractedUsername = jwtService.extractUsername(token);

    // then
    assertEquals("testUser", extractedUsername);
  }

  @Test
  void shouldValidateTokenSuccessfully() {
    // given
    given(userDetails.getUsername()).willReturn("testUser");
    String token = jwtService.generateToken(userDetails);

    // when
    boolean isValid = jwtService.isTokenValid(token, userDetails);

    // then
    assertTrue(isValid);
  }

  @Test
  @SneakyThrows
  void shouldDetectExpiredToken() {
    // given
    Method getSignInKeyMethod = JwtService.class.getDeclaredMethod("getSignInKey");
    getSignInKeyMethod.setAccessible(true);
    SecretKey key = (SecretKey) getSignInKeyMethod.invoke(jwtService);

    String expiredToken =
        Jwts.builder()
            .subject("testUser")
            .issuedAt(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
            .expiration(new Date(System.currentTimeMillis() - 1800000)) // 30 minutes ago
            .signWith(key)
            .compact();

    // when
    // then
    assertThatThrownBy(() -> jwtService.extractUsername(expiredToken))
        .isInstanceOf(ExpiredJwtException.class);
  }

  @Test
  void shouldDetectInvalidSignatureToken() {
    // given
    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
    String invalidToken =
        Jwts.builder()
            .subject("testUser")
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(key)
            .compact();

    // when
    // then
    assertThatThrownBy(() -> jwtService.extractUsername(invalidToken))
        .isInstanceOf(SignatureException.class);
  }

  @Test
  void shouldGenerateRefreshTokenSuccessfully() {
    // given
    given(userDetails.getUsername()).willReturn("testUser");

    // when
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    // then
    assertNotNull(refreshToken);
    assertFalse(refreshToken.isEmpty());
  }
}
