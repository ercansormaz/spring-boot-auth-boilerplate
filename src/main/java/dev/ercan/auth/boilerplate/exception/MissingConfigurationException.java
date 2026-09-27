package dev.ercan.auth.boilerplate.exception;

import lombok.Getter;

@Getter
public class MissingConfigurationException extends RuntimeException {

  public MissingConfigurationException(String message) {
    super(message);
  }

}
