package dev.ercan.auth.boilerplate.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
public class OtpRequest {

  @NotNull
  private UUID id;

  @NotEmpty
  private String value;

  @NotEmpty
  private String signature;

}
