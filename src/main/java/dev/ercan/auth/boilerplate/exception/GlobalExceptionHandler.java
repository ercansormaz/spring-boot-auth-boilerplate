package dev.ercan.auth.boilerplate.exception;

import dev.ercan.auth.boilerplate.dto.response.ErrorResponse;
import dev.ercan.auth.boilerplate.dto.ValidationErrorDto;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    List<ValidationErrorDto> validationErrors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(fieldError -> new ValidationErrorDto(fieldError.getField(), fieldError.getDefaultMessage()))
        .toList();

    return new ErrorResponse(ErrorType.REQUEST_VALIDATION_ERROR, validationErrors);
  }

  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(value = MissingConfigurationException.class)
  public ErrorResponse handleMissingConfigurationException(MissingConfigurationException ex) {
    log.error("[GLOBAL_EXCEPTION_HANDLER] [MISSING_CONFIGURATION] [MESSAGE={}]", ex.getMessage());
    return new ErrorResponse(ErrorType.INTERNAL_SERVER_ERROR);
  }

  @ResponseStatus(value = HttpStatus.NOT_IMPLEMENTED)
  @ExceptionHandler(value = UnsupportedStrategyException.class)
  public ErrorResponse handleUnsupportedStrategyException(UnsupportedStrategyException ex) {
    log.error("[GLOBAL_EXCEPTION_HANDLER] [UNSUPPORTED_STRATEGY] [MESSAGE={}]", ex.getMessage());
    return new ErrorResponse(ex.getErrorType());
  }

  @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(value = InvalidCredentialsException.class)
  public ErrorResponse handleInvalidCredentialsException(InvalidCredentialsException ex) {
    return new ErrorResponse(ex.getErrorType());
  }

  @ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
  @ExceptionHandler(value = RateLimitExceedException.class)
  public ErrorResponse handleRateLimitExceedException(RateLimitExceedException ex) {
    log.warn("[GLOBAL_EXCEPTION_HANDLER] [RATE_LIMIT_EXCEED] [TYPE={}]", ex.getErrorType().name());
    return new ErrorResponse(ex.getErrorType());
  }

}
