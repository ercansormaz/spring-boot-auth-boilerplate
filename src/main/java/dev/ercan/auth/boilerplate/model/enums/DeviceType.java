package dev.ercan.auth.boilerplate.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DeviceType {

  WEB,
  PHONE,
  TABLET,
  DESKTOP,
  TV,

  ;

  @JsonCreator
  public static DeviceType from(String value) {
    for (DeviceType type : DeviceType.values()) {
      if (type.name().equalsIgnoreCase(value)) {
        return type;
      }
    }
    return null;
  }

}
