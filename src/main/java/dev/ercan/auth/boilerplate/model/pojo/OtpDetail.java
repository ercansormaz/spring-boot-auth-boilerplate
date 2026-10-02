package dev.ercan.auth.boilerplate.model.pojo;

import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import java.time.Duration;

public record OtpDetail(String signature, String dataToVerify, String value, OtpFlowType flow, int length, Duration ttl,
                        long remainingRetryCount, Duration retryDelay) {

}
