package dev.ercan.auth.boilerplate.model.pojo;

import java.time.Duration;

public record RateLimitPolicy(int limit, Duration window) {

}
