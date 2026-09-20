package dev.ercan.auth.boilerplate.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.ercan.auth.boilerplate.model.enums.DevicePlatform;
import dev.ercan.auth.boilerplate.model.enums.DeviceType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceRequest {

  @NotEmpty
  private String identifier;

  @NotNull
  private DevicePlatform platform;

  @NotNull
  private DeviceType type;

  @NotEmpty
  private String model;

  @NotEmpty
  @Size(min = 2, max = 2, message = "must be 2 characters long")
  private String language;

  @NotEmpty
  @JsonProperty("app_version")
  private String appVersion;

  @NotEmpty
  @JsonProperty("os_version")
  private String osVersion;

}