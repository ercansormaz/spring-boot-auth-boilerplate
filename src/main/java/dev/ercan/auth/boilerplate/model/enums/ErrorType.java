package dev.ercan.auth.boilerplate.model.enums;

import lombok.Getter;

@Getter
public enum ErrorType {

  REQUEST_VALIDATION_ERROR(1001, "Request validation failed. Please check details."),
  UNSUPPORTED_STRATEGY(1002, "The requested feature is currently not supported."),

  INSUFFICIENT_AUTHORITY(1008, "You do not have permission to access this resource."),
  INVALID_OR_EXPIRED_TOKEN(1009, "Invalid or expired token. Please refresh token."),

  INVALID_REFRESH_TOKEN(1011, "Refresh token invalid. Please login again."),

  INVALID_GOOGLE_TOKEN(1012, "Invalid or expired Google token."),
  INVALID_APPLE_TOKEN(1013, "Invalid or expired Apple token."),
  INVALID_OTP(1014, "Invalid or expired OTP."),

  ANONYMOUS_LOGIN_LIMIT_EXCEED(3001, "Too many anonymous login. Please try again later."),
  REFRESH_TOKEN_SUCCESS_LIMIT_EXCEED(3002, "Too many refresh token login. Please try again later."),
  REFRESH_TOKEN_FAIL_LIMIT_EXCEED(3003, "Too many failed refresh token login. Please try again later."),
  APPLE_LOGIN_FAIL_LIMIT_EXCEED(3004, "Too many failed apple login. Please try again later."),
  GOOGLE_LOGIN_FAIL_LIMIT_EXCEED(3005, "Too many failed google login. Please try again later."),
  EMAIL_OTP_LIMIT_EXCEED(3006, "Too many requests. Please try again later."),

  ;

  private final int code;
  private final String message;

  ErrorType(int code, String message) {
    this.code = code;
    this.message = message;
  }

}