package dev.ercan.auth.boilerplate.exception;

import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {

  private final ErrorType errorType;

  public BadRequestException(ErrorType errorType) {
    this.errorType = errorType;
  }

}
