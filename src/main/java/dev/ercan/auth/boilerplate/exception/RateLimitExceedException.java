package dev.ercan.auth.boilerplate.exception;

import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import lombok.Getter;

@Getter
public class RateLimitExceedException extends RuntimeException {

  private final ErrorType errorType;

  public RateLimitExceedException(ErrorType errorType) {
    this.errorType = errorType;
  }

}
