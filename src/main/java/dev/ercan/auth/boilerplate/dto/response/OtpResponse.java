package dev.ercan.auth.boilerplate.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class OtpResponse {

  private UUID id;
  private String signature;
  private int length;
  @JsonProperty("expires_in")
  private long expiresIn;
  @JsonProperty("retry_in")
  private Long retryIn;
  private boolean retryable;

  public OtpResponse(OtpDetail otpDetail) {
    this.id = otpDetail.id();
    this.signature = otpDetail.signature();
    this.length = otpDetail.length();
    this.expiresIn = otpDetail.expiresIn();
    if (otpDetail.flow().isRetryable() && otpDetail.remainingRetryCount() > 0) {
      this.retryable = otpDetail.flow().isRetryable();
      this.retryIn = otpDetail.retryIn();
    }
  }

}
