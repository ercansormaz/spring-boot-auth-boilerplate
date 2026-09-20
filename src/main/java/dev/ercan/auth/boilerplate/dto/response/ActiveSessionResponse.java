package dev.ercan.auth.boilerplate.dto.response;

import dev.ercan.auth.boilerplate.model.enums.DevicePlatform;
import dev.ercan.auth.boilerplate.model.enums.DeviceType;
import java.time.Instant;

public record ActiveSessionResponse(String deviceId, DevicePlatform platform, DeviceType type, String model,
                                    Instant createdAt, boolean current) {

}
