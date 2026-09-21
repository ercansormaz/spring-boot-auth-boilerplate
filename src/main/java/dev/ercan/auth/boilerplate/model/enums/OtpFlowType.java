package dev.ercan.auth.boilerplate.model.enums;

import lombok.Getter;

@Getter
public enum OtpFlowType {

  AUTHENTICATION(true),

  ;

  private final boolean retryable;

  OtpFlowType(boolean retryable) {
    this.retryable = retryable;
  }

}
