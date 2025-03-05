package it.theapplegeek.spring_starter_pack.security.service;

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
  void shouldGenerateToken() {
    // Given
    given(userDetails.getUsername()).willReturn("testUser");

    // When
    String token = jwtService.generateToken(userDetails);

    // Then
    assertNotNull(token);
    assertFalse(token.isEmpty());
  }

  @Test
  void shouldExtractUsernameFromToken() {
    // Given
    when(userDetails.getUsername()).thenReturn("testUser");
    String token = jwtService.generateToken(userDetails);

    // When
    String extractedUsername = jwtService.extractUsername(token);

    // Then
    assertEquals("testUser", extractedUsername);
  }

  @Test
  void shouldValidateToken() {
    // Given
    given(userDetails.getUsername()).willReturn("testUser");
    String token = jwtService.generateToken(userDetails);

    // When
    boolean isValid = jwtService.isTokenValid(token, userDetails);

    // Then
    assertTrue(isValid);
  }

  @Test
  @SneakyThrows
  void shouldDetectExpiredToken() {
    // Given
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

    // When
    // Then
    assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(expiredToken));
  }

  @Test
  void shouldDetectInvalidSignatureToken() {
    // Given
    SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());
    String invalidToken =
        Jwts.builder()
            .subject("testUser")
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(key)
            .compact();

    // When
    // Then
    assertThrows(SignatureException.class, () -> jwtService.extractUsername(invalidToken));
  }

  @Test
  void shouldGenerateRefreshToken() {
    // Given
    given(userDetails.getUsername()).willReturn("testUser");

    // When
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    // Then
    assertNotNull(refreshToken);
    assertFalse(refreshToken.isEmpty());
  }
}
