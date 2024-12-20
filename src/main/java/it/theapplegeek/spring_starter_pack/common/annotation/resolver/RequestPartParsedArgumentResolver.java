package it.theapplegeek.spring_starter_pack.common.annotation.resolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.theapplegeek.spring_starter_pack.common.annotation.RequestPartParsed;
import it.theapplegeek.spring_starter_pack.common.exception.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Objects;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

public class RequestPartParsedArgumentResolver implements HandlerMethodArgumentResolver {
  private final ObjectMapper objectMapper;

  public RequestPartParsedArgumentResolver(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(RequestPartParsed.class);
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      @NonNull NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory) {
    RequestPartParsed requestPartParsed = parameter.getParameterAnnotation(RequestPartParsed.class);
    if (requestPartParsed == null) {
      return null;
    }

    String partName = requestPartParsed.value();
    MultipartHttpServletRequest multipartRequest =
        new StandardServletMultipartResolver()
            .resolveMultipart(
                Objects.requireNonNull(webRequest.getNativeRequest(HttpServletRequest.class)));
    String json = multipartRequest.getParameter(partName);

    if (json == null || json.isEmpty()) {
      throw new BadRequestException("Missing JSON string: " + partName);
    }

    JavaType javaType = getJavaType(parameter);
    try {
      return objectMapper.readValue(json, javaType);
    } catch (JsonProcessingException e) {
      throw new BadRequestException("Invalid JSON string: " + partName);
    }
  }

  @SuppressWarnings("unchecked")
  private JavaType getJavaType(MethodParameter parameter) {
    if (Collection.class.isAssignableFrom(parameter.getParameterType())) {
      ParameterizedType parameterizedType = (ParameterizedType) parameter.getGenericParameterType();
      Class<?> collectionType = (Class<?>) parameterizedType.getRawType();
      Class<?> elementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
      return objectMapper
          .getTypeFactory()
          .constructCollectionType((Class<? extends Collection>) collectionType, elementType);
    } else {
      return objectMapper.getTypeFactory().constructType(parameter.getGenericParameterType());
    }
  }
}
