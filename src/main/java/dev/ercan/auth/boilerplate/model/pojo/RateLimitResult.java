package dev.ercan.auth.boilerplate.model.pojo;

import java.time.Duration;

public record RateLimitResult(boolean consumed, long remainingTokens, Duration timeToWait) {

}
