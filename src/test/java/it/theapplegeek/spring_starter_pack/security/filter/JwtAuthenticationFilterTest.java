package it.theapplegeek.spring_starter_pack.security.filter;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import it.theapplegeek.spring_starter_pack.security.service.JwtService;
import it.theapplegeek.spring_starter_pack.token.model.Token;
import it.theapplegeek.spring_starter_pack.token.repository.TokenRepository;
import it.theapplegeek.spring_starter_pack.user.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
  @Mock private JwtService jwtService;
  @Mock private UserDetailsService userDetailsService;
  @Mock private TokenRepository tokenRepository;
  @Mock private FilterChain filterChain;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @InjectMocks private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  void shouldIgnoreAuthEndpoints() throws ServletException, IOException {
    // given
    given(request.getServletPath()).willReturn("/api/auth/login");

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(request, response);
    verifyNoInteractions(jwtService, userDetailsService, tokenRepository);
  }

  @Test
  void shouldIgnoreRequestsWithoutAuthorizationHeader() throws ServletException, IOException {
    // given
    given(request.getServletPath()).willReturn("/api/protected");
    given(request.getHeader("Authorization")).willReturn(null);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(request, response);
    verifyNoInteractions(jwtService, userDetailsService, tokenRepository);
  }

  @Test
  void shouldIgnoreRequestsWithWrongAuthorizationHeader() throws ServletException, IOException {
    // given
    given(request.getServletPath()).willReturn("/api/protected");
    given(request.getHeader("Authorization")).willReturn("NotBearer");

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(request, response);
    verifyNoInteractions(jwtService, userDetailsService, tokenRepository);
  }

  @Test
  void shouldIgnoreRequestsWithInvalidToken() throws ServletException, IOException {
    // given
    given(request.getServletPath()).willReturn("/api/protected");
    given(request.getHeader("Authorization")).willReturn("Bearer invalid_token");
    given(jwtService.extractUsername("invalid_token")).willReturn(null);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(request, response);
    verifyNoInteractions(userDetailsService, tokenRepository);
  }

  @Test
  void shouldNotAuthenticateUserWithInvalidToken() throws ServletException, IOException {
    // given
    String invalidToken = "not_valid_jwt";
    String username = "testUser";
    UserDetails userDetails = mock(User.class);
    SecurityContext securityContext = mock(SecurityContext.class);

    SecurityContextHolder.setContext(securityContext);

    given(request.getServletPath()).willReturn("/api/protected");
    given(request.getHeader("Authorization")).willReturn("Bearer " + invalidToken);
    given(jwtService.extractUsername(invalidToken)).willReturn(username);
    given(userDetailsService.loadUserByUsername(username)).willReturn(userDetails);
    given(securityContext.getAuthentication()).willReturn(null);
    given(tokenRepository.findByToken(invalidToken))
        .willReturn(Optional.of(Token.builder().token(invalidToken).userId(1L).build()));
    given(jwtService.isTokenValid(invalidToken, userDetails)).willReturn(false);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(request, response);
    verify(securityContext, never())
        .setAuthentication(any(UsernamePasswordAuthenticationToken.class));
  }

  @Test
  void shouldAuthenticateUserWithValidToken() throws ServletException, IOException {
    // given
    String validToken = "valid_jwt";
    String username = "testUser";
    UserDetails userDetails = mock(User.class);
    SecurityContext securityContext = mock(SecurityContext.class);

    SecurityContextHolder.setContext(securityContext);

    given(request.getServletPath()).willReturn("/api/protected");
    given(request.getHeader("Authorization")).willReturn("Bearer " + validToken);
    given(jwtService.extractUsername(validToken)).willReturn(username);
    given(userDetailsService.loadUserByUsername(username)).willReturn(userDetails);
    given(securityContext.getAuthentication()).willReturn(null);
    given(tokenRepository.findByToken(validToken))
        .willReturn(Optional.of(Token.builder().token(validToken).userId(1L).build()));
    given(jwtService.isTokenValid(validToken, userDetails)).willReturn(true);

    // when
    jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

    // then
    verify(filterChain, times(1)).doFilter(request, response);
    verify(securityContext, times(1))
        .setAuthentication(any(UsernamePasswordAuthenticationToken.class));
  }
}
