package dev.ercan.auth.boilerplate.service.port;

import java.time.Duration;

public interface RateLimiter {

  boolean tryConsume(String key, int limit, Duration window);
  void rollback(String key, int limit, Duration window);

}
