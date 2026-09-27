package dev.ercan.auth.boilerplate.model.pojo;

import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import java.util.UUID;

public record OtpDetail(UUID id, String signature, String dataToVerify, String value, OtpFlowType flow, int length,
                        long expiresIn, long remainingRetryCount, long retryIn) {

}
