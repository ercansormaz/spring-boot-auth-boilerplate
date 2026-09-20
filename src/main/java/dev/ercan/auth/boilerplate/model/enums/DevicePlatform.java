package dev.ercan.auth.boilerplate.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DevicePlatform {

  WEB,
  IOS,
  ANDROID,
  MACOS,
  WINDOWS,
  LINUX,
  FUCHSIA,

  ;

  @JsonCreator
  public static DevicePlatform from(String value) {
    for (DevicePlatform platform : DevicePlatform.values()) {
      if (platform.name().equalsIgnoreCase(value)) {
        return platform;
      }
    }
    return null;
  }

}
