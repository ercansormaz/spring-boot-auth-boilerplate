package dev.ercan.auth.boilerplate.exception;

import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import lombok.Getter;

@Getter
public class InvalidCredentialsException extends RuntimeException {

  private final ErrorType errorType;

  public InvalidCredentialsException(ErrorType errorType) {
    this.errorType = errorType;
  }

}