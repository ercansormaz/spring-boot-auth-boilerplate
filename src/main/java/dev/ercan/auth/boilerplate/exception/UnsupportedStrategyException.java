package dev.ercan.auth.boilerplate.exception;

import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import lombok.Getter;

@Getter
public class UnsupportedStrategyException extends RuntimeException {

  private final ErrorType errorType;

  public UnsupportedStrategyException(ErrorType errorType, Class<?> clazz, Object key) {
    super(String.format("No implementation found for strategy [%s] with key [%s]", clazz.getSimpleName(), key));
    this.errorType = errorType;
  }

}