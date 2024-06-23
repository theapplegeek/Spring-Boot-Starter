package it.theapplegeek.spring_starter_pack.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.AllArgsConstructor;
import it.theapplegeek.spring_starter_pack.resolver.ProvideUserLoggedArgumentResolver;
import it.theapplegeek.spring_starter_pack.resolver.RequestPartParsedArgumentResolver;
import it.theapplegeek.spring_starter_pack.security.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@AllArgsConstructor
public class WebConfig implements WebMvcConfigurer {
  private final ObjectMapper objectMapper;
  private final JwtService jwtService;

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(new RequestPartParsedArgumentResolver(objectMapper));
    resolvers.add(new ProvideUserLoggedArgumentResolver(jwtService));
  }
}
