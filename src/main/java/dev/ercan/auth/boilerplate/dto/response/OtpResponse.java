package dev.ercan.auth.boilerplate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class OtpResponse {

  private String signature;
  private int length;
  @JsonProperty("expires_in")
  private long expiresIn;
  @JsonProperty("retry_in")
  private Long retryIn;
  private boolean retryable;

  public OtpResponse(OtpDetail otpDetail) {
    this.signature = otpDetail.signature();
    this.length = otpDetail.length();
    this.expiresIn = otpDetail.ttl().toSeconds();
    if (otpDetail.flow().isRetryable() && otpDetail.remainingRetryCount() > 0) {
      this.retryable = true;
      this.retryIn = otpDetail.retryDelay().toSeconds();
    }
  }

}
