package it.theapplegeek.spring_starter_pack.auth.service;

import it.theapplegeek.spring_starter_pack.auth.error.AuthMessage;
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
import jakarta.transaction.Transactional;
import java.time.ZoneId;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
  private final UserRepository userRepository;
  private final TokenRepository tokenRepository;
  private final JwtService jwtService;
  private final EmailService emailService;
  private final AuthenticationManager authenticationManager;
  private final PasswordEncoder passwordEncoder;

  public JwtResponse authenticate(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
    User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
    String accessToken = jwtService.generateToken(user);
    String refreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(user, accessToken);
    return JwtResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();
  }

  private void saveUserToken(User user, String jwtToken) {
    Token token =
        Token.builder()
            .userId(user.getId())
            .token(jwtToken)
            .tokenType(TokenType.BEARER)
            .revoked(false)
            .expiration(
                jwtService
                    .extractExpiration(jwtToken)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime())
            .build();
    tokenRepository.save(token);
  }

  public JwtResponse refreshToken(String refreshToken) {
    final String username = jwtService.extractUsername(refreshToken);
    if (username == null) throw new UnauthorizedException(AuthMessage.INVALID_TOKEN);
    User user = this.userRepository.findByUsername(username).orElseThrow();
    if (!jwtService.isTokenValid(refreshToken, user))
      throw new UnauthorizedException(AuthMessage.INVALID_TOKEN);
    String accessToken = jwtService.generateToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user);
    saveUserToken(user, accessToken);
    return JwtResponse.builder().accessToken(accessToken).refreshToken(newRefreshToken).build();
  }

  public void logout(String token) {
    Token storedToken = tokenRepository.findByToken(token).orElse(null);
    if (storedToken == null) return;
    storedToken.setRevoked(true);
    tokenRepository.save(storedToken);
    SecurityContextHolder.clearContext();
  }

  public void revokeAllTokensOfUser(Long userId) {
    tokenRepository.revokeAllByUserIdAndType(userId, TokenType.BEARER);
  }

  public void forgotPassword(String email) {
    Optional<User> userOptional = userRepository.findByEmailAndEnabledIsTrue(email);
    if (userOptional.isEmpty()) return;
    User user = userOptional.get();
    String token = jwtService.generateResetPasswordToken(user);
    Token resetToken =
        Token.builder()
            .userId(user.getId())
            .token(token)
            .tokenType(TokenType.RESET_PASSWORD)
            .revoked(false)
            .expiration(
                jwtService
                    .extractExpiration(token)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime())
            .build();
    tokenRepository.save(resetToken);
    emailService.sendResetPasswordEmail(user, token);
  }

  @Transactional
  public void resetPassword(String token, String password) {
    Token resetToken =
        tokenRepository
            .findByTokenAndTokenTypeAndRevokedIsFalse(token, TokenType.RESET_PASSWORD)
            .orElseThrow(() -> new UnauthorizedException(AuthMessage.INVALID_TOKEN));
    User user =
        userRepository
            .findById(resetToken.getUserId())
            .orElseThrow(() -> new UnauthorizedException(AuthMessage.INVALID_TOKEN));
    if (!jwtService.isTokenValid(token, user))
      throw new UnauthorizedException(AuthMessage.INVALID_TOKEN);
    user.setPassword(passwordEncoder.encode(password));
    resetToken.setRevoked(true);
    userRepository.save(user);
    tokenRepository.save(resetToken);
  }
}
