package it.theapplegeek.spring_starter_pack.common.annotation.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import it.theapplegeek.spring_starter_pack.common.annotation.ProvideUserLogged;
import it.theapplegeek.spring_starter_pack.common.exception.UnauthorizedException;
import it.theapplegeek.spring_starter_pack.security.service.JwtService;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class ProvideUserLoggedArgumentResolver implements HandlerMethodArgumentResolver {
  private final JwtService jwtService;

  public ProvideUserLoggedArgumentResolver(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(ProvideUserLogged.class);
  }

  @Override
  public Object resolveArgument(
      @NonNull MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
    String token = getTokenFromRequest(request);

    if (token == null) {
      throw new UnauthorizedException("Unauthorized");
    }

    try {
      return jwtService.generateUserLogged(token);
    } catch (Exception e) {
      log.error("Error on get user logged from token", e);
      throw new UnauthorizedException("Unauthorized");
    }
  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
