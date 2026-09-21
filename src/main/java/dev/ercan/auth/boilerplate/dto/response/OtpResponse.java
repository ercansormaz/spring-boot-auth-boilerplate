package dev.ercan.auth.boilerplate.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class OtpResponse {

  private UUID id;
  private String signature;
  private int length;
  @JsonProperty("expires_in")
  private long expiresIn;
  private boolean retryable;

  public OtpResponse(OtpDetail otpDetail) {
    this.id = otpDetail.id();
    this.signature = otpDetail.signature();
    this.length = otpDetail.length();
    this.expiresIn = otpDetail.expiresIn();
    this.retryable = otpDetail.flow().isRetryable();
  }

}
