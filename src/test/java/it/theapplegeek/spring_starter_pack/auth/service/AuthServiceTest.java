package it.theapplegeek.spring_starter_pack.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import it.theapplegeek.spring_starter_pack.common.exception.UnauthorizedException;
import it.theapplegeek.spring_starter_pack.common.payload.JwtResponse;
import it.theapplegeek.spring_starter_pack.common.payload.LoginRequest;
import it.theapplegeek.spring_starter_pack.email.service.EmailService;
import it.theapplegeek.spring_starter_pack.security.service.JwtService;
import it.theapplegeek.spring_starter_pack.token.model.Token;
import it.theapplegeek.spring_starter_pack.token.model.TokenType;
import it.theapplegeek.spring_starter_pack.token.repository.TokenRepository;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock private UserRepository userRepository;
  @Mock private TokenRepository tokenRepository;
  @Mock private JwtService jwtService;
  @Mock private EmailService emailService;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private PasswordEncoder passwordEncoder;
  @InjectMocks private AuthService authService;

  private final User user =
      User.builder()
          .id(1L)
          .username("testUser")
          .email("test@example.com")
          .password("encodedPassword")
          .build();
  private final String accessToken = "access-token";
  private final String refreshToken = "refresh-token";
  private final String resetPasswordToken = "reset-token";
  private final String password = "newPassword";

  @Test
  void shouldAuthenticateAndReturnJwtTokens() {
    // Given
    LoginRequest loginRequest = new LoginRequest("testUser", "password");
    given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
    given(jwtService.generateToken(user)).willReturn(accessToken);
    given(jwtService.generateRefreshToken(user)).willReturn(refreshToken);
    given(jwtService.extractExpiration(accessToken))
        .willReturn(
            new Date(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli()));

    // When
    JwtResponse response = authService.authenticate(loginRequest);

    // Then
    verify(tokenRepository, times(1)).save(any(Token.class));
    assertNotNull(response);
    assertEquals(accessToken, response.getAccessToken());
    assertEquals(refreshToken, response.getRefreshToken());
  }

  @Test
  void shouldRefreshToken() {
    // Given
    given(jwtService.extractUsername(refreshToken)).willReturn("testUser");
    given(userRepository.findByUsername("testUser")).willReturn(Optional.of(user));
    given(jwtService.isTokenValid(refreshToken, user)).willReturn(true);
    given(jwtService.generateToken(user)).willReturn(accessToken);
    given(jwtService.generateRefreshToken(user)).willReturn("new-refresh-token");
    given(jwtService.extractExpiration(accessToken))
        .willReturn(
            new Date(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli()));

    // When
    JwtResponse response = authService.refreshToken(refreshToken);

    // Then
    verify(tokenRepository, times(1)).save(any(Token.class));
    assertNotNull(response);
    assertEquals(accessToken, response.getAccessToken());
    assertEquals("new-refresh-token", response.getRefreshToken());
  }

  @Test
  void shouldThrowExceptionForInvalidRefreshToken() {
    // Given
    given(jwtService.extractUsername(refreshToken)).willReturn(null);

    // When
    // Then
    assertThrows(UnauthorizedException.class, () -> authService.refreshToken(refreshToken));
  }

  @Test
  void shouldThrowExceptionForNotStoredRefreshToken() {
    // Given
    given(jwtService.extractUsername(refreshToken)).willReturn("admin");
    given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));
    given(jwtService.isTokenValid(refreshToken, user)).willReturn(false);

    // When
    // Then
    assertThrows(UnauthorizedException.class, () -> authService.refreshToken(refreshToken));
  }

  @Test
  void shouldLogoutUser() {
    // Given
    Token storedToken = new Token();
    storedToken.setToken(accessToken);
    storedToken.setRevoked(false);

    given(tokenRepository.findByToken(accessToken)).willReturn(Optional.of(storedToken));

    // When
    authService.logout(accessToken);

    // Then
    verify(tokenRepository, times(1)).save(storedToken);
    assertTrue(storedToken.getRevoked());
    assertNull(SecurityContextHolder.getContext().getAuthentication());
  }

  @Test
  void shouldRevokeAllTokensForUser() {
    // When
    authService.revokeAllTokensOfUser(1L);

    // Then
    verify(tokenRepository, times(1)).revokeAllByUserIdAndType(1L, TokenType.BEARER);
  }

  @Test
  void shouldSendResetPasswordEmail() {
    // Given
    given(userRepository.findByEmailAndEnabledIsTrue("test@example.com"))
        .willReturn(Optional.of(user));
    given(jwtService.generateResetPasswordToken(user)).willReturn(resetPasswordToken);
    given(jwtService.extractExpiration(resetPasswordToken))
        .willReturn(
            new Date(LocalDateTime.now().plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli()));

    // When
    authService.forgotPassword("test@example.com");

    // Then
    verify(tokenRepository, times(1)).save(any(Token.class));
    verify(emailService, times(1)).sendResetPasswordEmail(user, resetPasswordToken);
  }

  @Test
  void shouldNotSendResetPasswordEmail() {
    // Given
    given(userRepository.findByEmailAndEnabledIsTrue("nonexistent@example.com"))
        .willReturn(Optional.empty());

    // When
    authService.forgotPassword("nonexistent@example.com");

    // Then
    verify(tokenRepository, never()).save(any(Token.class));
    verify(emailService, never()).sendResetPasswordEmail(any(User.class), anyString());
  }

  @Test
  void shouldResetPassword() {
    // Given
    Token resetToken = new Token();
    resetToken.setUserId(1L);
    resetToken.setToken(resetPasswordToken);
    resetToken.setTokenType(TokenType.RESET_PASSWORD);
    resetToken.setRevoked(false);

    given(
            tokenRepository.findByTokenAndTokenTypeAndRevokedIsFalse(
                resetPasswordToken, TokenType.RESET_PASSWORD))
        .willReturn(Optional.of(resetToken));
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(jwtService.isTokenValid(resetPasswordToken, user)).willReturn(true);
    given(passwordEncoder.encode(password)).willReturn("encodedNewPassword");

    // When
    authService.resetPassword(resetPasswordToken, password);

    // Then
    assertEquals("encodedNewPassword", user.getPassword());
    assertTrue(resetToken.getRevoked());
    verify(userRepository, times(1)).save(user);
    verify(tokenRepository, times(1)).save(resetToken);
  }

  @Test
  void shouldThrowExceptionForInvalidResetToken() {
    // Given
    given(
            tokenRepository.findByTokenAndTokenTypeAndRevokedIsFalse(
                resetPasswordToken, TokenType.RESET_PASSWORD))
        .willReturn(Optional.empty());

    // When & Then
    assertThrows(
        UnauthorizedException.class, () -> authService.resetPassword(resetPasswordToken, password));
    verify(userRepository, never()).save(any());
    verify(tokenRepository, never()).save(any());
  }

  @Test
  void shouldNotResetPasswordForNotExistingUser() {
    // Given
    Token resetToken = new Token();
    resetToken.setUserId(1L);
    resetToken.setToken(resetPasswordToken);
    resetToken.setTokenType(TokenType.RESET_PASSWORD);
    resetToken.setRevoked(false);

    given(
            tokenRepository.findByTokenAndTokenTypeAndRevokedIsFalse(
                resetPasswordToken, TokenType.RESET_PASSWORD))
        .willReturn(Optional.of(resetToken));
    given(userRepository.findById(1L)).willReturn(Optional.empty());

    // When
    // Then
    assertThrows(
        UnauthorizedException.class, () -> authService.resetPassword(resetPasswordToken, password));
    verify(userRepository, never()).save(any());
    verify(tokenRepository, never()).save(any());
  }

  @Test
  void shouldNotResetPasswordForNotStoredResetToken() {
    // Given
    Token resetToken = new Token();
    resetToken.setUserId(1L);
    resetToken.setToken(resetPasswordToken);
    resetToken.setTokenType(TokenType.RESET_PASSWORD);
    resetToken.setRevoked(false);

    given(
            tokenRepository.findByTokenAndTokenTypeAndRevokedIsFalse(
                resetPasswordToken, TokenType.RESET_PASSWORD))
        .willReturn(Optional.of(resetToken));
    given(userRepository.findById(1L)).willReturn(Optional.of(user));
    given(jwtService.isTokenValid(resetPasswordToken, user)).willReturn(false);

    // When
    // Then
    assertThrows(
        UnauthorizedException.class, () -> authService.resetPassword(resetPasswordToken, password));
    verify(userRepository, never()).save(any());
    verify(tokenRepository, never()).save(any());
  }
}
