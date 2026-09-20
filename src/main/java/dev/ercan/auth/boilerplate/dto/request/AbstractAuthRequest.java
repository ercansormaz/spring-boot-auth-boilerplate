package dev.ercan.auth.boilerplate.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class AbstractAuthRequest {

  @Valid
  @NotNull
  private DeviceRequest device;

  private boolean remember;

}
