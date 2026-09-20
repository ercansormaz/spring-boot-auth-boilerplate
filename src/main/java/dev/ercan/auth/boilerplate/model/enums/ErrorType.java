package dev.ercan.auth.boilerplate.model.enums;

import lombok.Getter;

@Getter
public enum ErrorType {

  REQUEST_VALIDATION_ERROR(1001, "Request validation failed. Please check details."),
  UNSUPPORTED_STRATEGY(1002, "The requested feature is currently not supported."),

  INSUFFICIENT_AUTHORITY(1008, "You do not have permission to access this resource."),
  INVALID_OR_EXPIRED_TOKEN(1009, "Invalid or expired token. Please refresh token."),

  INVALID_REFRESH_TOKEN(1011, "Refresh token invalid. Please login again."),

  ;

  private final int code;
  private final String message;

  ErrorType(int code, String message) {
    this.code = code;
    this.message = message;
  }

}