package it.theapplegeek.spring_starter_pack.security.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import it.theapplegeek.spring_starter_pack.permission.model.Permission;
import it.theapplegeek.spring_starter_pack.role.model.Role;
import it.theapplegeek.spring_starter_pack.role.model.RolePermission;
import it.theapplegeek.spring_starter_pack.security.model.UserLogged;
import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.model.UserRole;
import java.util.*;
import java.util.function.Function;
import javax.crypto.SecretKey;
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

  public String generateToken(User userDetails) {
    HashMap<String, Object> claims = new HashMap<>();
    claims.put("id", userDetails.getId());
    claims.put("email", userDetails.getEmail());
    claims.put("name", userDetails.getName());
    claims.put("surname", userDetails.getSurname());
    List<Map<String, Object>> roles = new ArrayList<>();
    List<Map<String, Object>> permissions = new ArrayList<>();
    for (UserRole userRole : userDetails.getUserRoles()) {
      HashMap<String, Object> roleMap = new HashMap<>();
      roleMap.put("id", userRole.getId().getRoleId());
      roleMap.put("name", userRole.getRole().getName());
      roles.add(roleMap);

      for (RolePermission rolePermission : userRole.getRole().getRolePermissions()) {
        HashMap<String, Object> permissionMap = new HashMap<>();
        permissionMap.put("id", rolePermission.getId().getPermissionId());
        permissionMap.put("name", rolePermission.getPermission().getName());
        permissions.add(permissionMap);
      }
    }
    claims.put("roles", roles);
    claims.put("permissions", permissions);
    claims.put("jti", UUID.randomUUID().toString());
    return generateToken(claims, userDetails);
  }

  @SuppressWarnings("unchecked")
  public UserLogged generateUserLogged(String token) {
    final Claims claims = extractAllClaims(token);
    ArrayList<LinkedHashMap<String, Object>> rolesMapList = claims.get("roles", ArrayList.class);
    List<Role> roles =
        rolesMapList.stream()
            .map(
                roleMap ->
                    Role.builder()
                        .id(Long.valueOf(roleMap.get("id").toString()))
                        .name(roleMap.get("name").toString())
                        .build())
            .toList();

    ArrayList<LinkedHashMap<String, Object>> permissionsMapList =
        claims.get("permissions", ArrayList.class);
    List<Permission> permissions =
        permissionsMapList.stream()
            .map(
                permissionMap ->
                    Permission.builder()
                        .id(Long.valueOf(permissionMap.get("id").toString()))
                        .name(permissionMap.get("name").toString())
                        .build())
            .toList();

    return UserLogged.builder()
        .id(claims.get("id", Long.class))
        .email(claims.get("email", String.class))
        .name(claims.get("name", String.class))
        .surname(claims.get("surname", String.class))
        .username(claims.getSubject())
        .roles(roles)
        .permissions(permissions)
        .build();
  }

  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails, jwtExpiration);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    HashMap<String, Object> claims = new HashMap<>();
    claims.put("jti", UUID.randomUUID().toString());
    return buildToken(claims, userDetails, refreshExpiration);
  }

  public String generateResetPasswordToken(UserDetails userDetails) {
    HashMap<String, Object> claims = new HashMap<>();
    claims.put("jti", UUID.randomUUID().toString());
    return buildToken(claims, userDetails, resetPasswordExpiration);
  }

  private String buildToken(
      Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSignInKey())
        .compact();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
