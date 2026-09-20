package dev.ercan.auth.boilerplate.model.enums;

import lombok.Getter;

@Getter
public enum RateLimitType {

  ANONYMOUS_LOGIN_SUCCESS(ErrorType.ANONYMOUS_LOGIN_LIMIT_EXCEED),
  REFRESH_TOKEN_SUCCESS(ErrorType.REFRESH_TOKEN_SUCCESS_LIMIT_EXCEED),
  REFRESH_TOKEN_FAIL(ErrorType.REFRESH_TOKEN_FAIL_LIMIT_EXCEED),
  APPLE_LOGIN_FAIL(ErrorType.APPLE_LOGIN_FAIL_LIMIT_EXCEED),
  GOOGLE_LOGIN_FAIL(ErrorType.GOOGLE_LOGIN_FAIL_LIMIT_EXCEED),

  ;

  private final ErrorType errorType;

  RateLimitType(ErrorType errorType) {
    this.errorType = errorType;
  }

}