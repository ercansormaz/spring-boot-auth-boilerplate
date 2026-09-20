package dev.ercan.auth.boilerplate.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleAuthRequest extends AbstractAuthRequest {

  @NotEmpty
  @JsonProperty("id_token")
  private String token;

}
