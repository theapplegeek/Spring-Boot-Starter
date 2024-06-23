package it.theapplegeek.spring_starter_pack.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.security.model.UserLogged;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  @Value("${application.security.jwt.secret-key}")
  private String secretKey;

  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration;

  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshExpiration;

  @Value("${application.security.jwt.refresh-token.expiration}")
  private long resetPasswordExpiration;

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateToken(User userDetails) {
    HashMap<String, Object> claims = new HashMap<>();
    claims.put("id", userDetails.getId());
    claims.put("email", userDetails.getEmail());
    claims.put("name", userDetails.getName());
    claims.put("surname", userDetails.getSurname());
    HashMap<String, Object> role = new HashMap<>();
    role.put("id", userDetails.getRole().getId());
    role.put("name", userDetails.getRole().getName());
    claims.put("role", role);
    return generateToken(claims, userDetails);
  }

  public UserLogged generateUserLogged(String token) {
    final Claims claims = extractAllClaims(token);
    LinkedHashMap<String, Object> roleMap = claims.get("role", LinkedHashMap.class);
    Role role =
            Role.builder().id(Long.valueOf(roleMap.get("id").toString())).name(roleMap.get("name").toString()).build();
    return UserLogged.builder()
        .id(claims.get("id", Long.class))
        .email(claims.get("email", String.class))
        .name(claims.get("name", String.class))
        .surname(claims.get("surname", String.class))
        .username(claims.getSubject())
        .role(role)
        .build();
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, refreshExpiration);
  }

  public String generateResetPasswordToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails, resetPasswordExpiration);
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
