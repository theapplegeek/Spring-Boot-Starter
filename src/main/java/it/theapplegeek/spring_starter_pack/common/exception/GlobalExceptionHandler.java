package it.theapplegeek.spring_starter_pack.common.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import it.theapplegeek.spring_starter_pack.common.payload.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(
      BadRequestException ex, WebRequest request) {
    log.error("Bad Request", ex);
    return generateExceptionResponse(ex, request, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(
      HandlerMethodValidationException ex, WebRequest request) {
    log.error("HandlerMethodValidationException", ex);
    Object[] detailMessageArguments = ex.getDetailMessageArguments();
    String errorMessage =
        detailMessageArguments != null
                && detailMessageArguments.length > 0
                && detailMessageArguments[0] instanceof String message
            ? message
            : "Validation error";
    return generateExceptionResponse(request, HttpStatus.BAD_REQUEST, errorMessage);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleBadRequestException(
      HttpMessageNotReadableException ex, WebRequest request) {
    log.error("HttpMessageNotReadableException", ex);
    String message = ex.getMessage().substring(18);
    return generateExceptionResponse(request, HttpStatus.BAD_REQUEST, message);
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      BindException ex, WebRequest request) {
    log.error("Validation error", ex);
    return generateExceptionResponse(request, HttpStatus.BAD_REQUEST, generateValidationError(ex));
  }

  private String generateValidationError(BindException ex) {
    StringBuilder sb = new StringBuilder();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(
            e -> sb.append(e.getField()).append(": ").append(e.getDefaultMessage()).append("; "));
    return sb.substring(0, sb.length() - 2);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundException(
      NotFoundException ex, WebRequest request) {
    log.error("Not Found", ex);
    return generateExceptionResponse(ex, request, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> handleForbiddenException(
      RuntimeException ex, WebRequest request) {
    log.error("Access Denied", ex);
    return generateExceptionResponse(ex, request, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler({
    AuthenticationException.class,
    UnauthorizedException.class,
    ExpiredJwtException.class,
    MalformedJwtException.class,
  })
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      RuntimeException ex, WebRequest request) {
    log.error("Unauthorized", ex);
    return generateExceptionResponse(ex, request, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({RuntimeException.class, InternalServerErrorException.class})
  public ResponseEntity<ErrorResponse> handleInternalServerException(
      RuntimeException ex, WebRequest request) {
    log.error("Internal server error", ex);
    return generateExceptionResponse(
        request, HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
  }

  private static ResponseEntity<ErrorResponse> generateExceptionResponse(
      RuntimeException ex, WebRequest request, HttpStatus status) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .path(request.getDescription(false).substring(4))
            .error(status.getReasonPhrase())
            .status(status.value())
            .message(ex.getMessage())
            .build();

    return new ResponseEntity<>(errorResponse, status);
  }

  private static ResponseEntity<ErrorResponse> generateExceptionResponse(
      WebRequest request, HttpStatus status, String message) {
    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .path(request.getDescription(false).substring(4))
            .error(status.getReasonPhrase())
            .status(status.value())
            .message(message)
            .build();

    return new ResponseEntity<>(errorResponse, status);
  }
}
