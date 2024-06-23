package it.theapplegeek.spring_starter_pack.auth.controller;

import java.util.Map;
import lombok.AllArgsConstructor;
import it.theapplegeek.spring_starter_pack.common.exception.BadRequestException;
import it.theapplegeek.spring_starter_pack.common.payload.JwtResponse;
import it.theapplegeek.spring_starter_pack.common.payload.LoginRequest;
import it.theapplegeek.spring_starter_pack.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {
  private final AuthService authService;

  @PostMapping("login")
  public JwtResponse authenticate(@RequestBody LoginRequest request) {
    return authService.authenticate(request);
  }

  @PostMapping("refresh-token")
  public JwtResponse refreshToken(@RequestHeader("authorization") String token) {
    return authService.refreshToken(token.substring(7));
  }

  @PostMapping("forgot-password")
  public void forgotPassword(@RequestBody Map<String, String> body) {
    String email = body.get("email");
    if (email == null) throw new BadRequestException("Invalid email address");
    authService.forgotPassword(email);
  }

  @PostMapping("reset-password")
  public void resetPassword(@RequestBody Map<String, String> body) {
    String token = body.get("token");
    String password = body.get("newPassword");
    if (token == null || password == null)
      throw new BadRequestException("Invalid token or password");
    authService.resetPassword(token, password);
  }

  @PostMapping("logout")
  public void logout(@RequestHeader("authorization") String token) {
    authService.logout(token.substring(7));
  }
}
