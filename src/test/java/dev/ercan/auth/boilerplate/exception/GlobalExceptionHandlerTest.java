package dev.ercan.auth.boilerplate.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.dto.response.ErrorResponse;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.HashMap;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleMethodArgumentNotValidExceptionReturnsValidationErrors() {
    MapBindingResult bindingResult = new MapBindingResult(new HashMap<>(), "target");
    bindingResult.addError(new FieldError("target", "email", "must not be blank"));
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

    ErrorResponse response = handler.handleMethodArgumentNotValidException(ex);

    assertEquals(ErrorType.REQUEST_VALIDATION_ERROR.getCode(), response.getCode());
    assertNotNull(response.getDetails());
    assertEquals(1, response.getDetails().size());
    assertEquals("email", response.getDetails().getFirst().field());
    assertEquals("must not be blank", response.getDetails().getFirst().error());
  }

  @Test
  void handleConstraintViolationReturnsValidationErrors() {
    ConstraintViolation<?> violation = mock(ConstraintViolation.class);
    Path path = mock(Path.class);
    when(path.toString()).thenReturn("getOtp.request.email");
    when(violation.getPropertyPath()).thenReturn(path);
    when(violation.getMessage()).thenReturn("must be a well-formed email address");

    ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

    ErrorResponse response = handler.handleConstraintViolation(ex);

    assertEquals(ErrorType.REQUEST_VALIDATION_ERROR.getCode(), response.getCode());
    assertNotNull(response.getDetails());
    assertEquals(1, response.getDetails().size());
    assertEquals("email", response.getDetails().getFirst().field());
    assertEquals("must be a well-formed email address", response.getDetails().getFirst().error());
  }

  @Test
  void handleMissingConfigurationExceptionReturnsInternalServerError() {
    MissingConfigurationException ex = new MissingConfigurationException("Missing property");

    ErrorResponse response = handler.handleMissingConfigurationException(ex);

    assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getCode(), response.getCode());
    assertEquals(ErrorType.INTERNAL_SERVER_ERROR.getMessage(), response.getMessage());
  }

  @Test
  void handleUnsupportedStrategyExceptionReturnsErrorType() {
    UnsupportedStrategyException ex = new UnsupportedStrategyException(
        ErrorType.UNSUPPORTED_STRATEGY, String.class, "DUMMY");

    ErrorResponse response = handler.handleUnsupportedStrategyException(ex);

    assertEquals(ErrorType.UNSUPPORTED_STRATEGY.getCode(), response.getCode());
    assertEquals(ErrorType.UNSUPPORTED_STRATEGY.getMessage(), response.getMessage());
  }

  @Test
  void handleInvalidCredentialsExceptionReturnsErrorType() {
    InvalidCredentialsException ex = new InvalidCredentialsException(ErrorType.INVALID_OTP);

    ErrorResponse response = handler.handleInvalidCredentialsException(ex);

    assertEquals(ErrorType.INVALID_OTP.getCode(), response.getCode());
    assertEquals(ErrorType.INVALID_OTP.getMessage(), response.getMessage());
  }

  @Test
  void handleRateLimitExceedExceptionReturnsErrorType() {
    RateLimitExceedException ex = new RateLimitExceedException(ErrorType.EMAIL_OTP_LIMIT_EXCEED);

    ErrorResponse response = handler.handleRateLimitExceedException(ex);

    assertEquals(ErrorType.EMAIL_OTP_LIMIT_EXCEED.getCode(), response.getCode());
    assertEquals(ErrorType.EMAIL_OTP_LIMIT_EXCEED.getMessage(), response.getMessage());
  }
}
