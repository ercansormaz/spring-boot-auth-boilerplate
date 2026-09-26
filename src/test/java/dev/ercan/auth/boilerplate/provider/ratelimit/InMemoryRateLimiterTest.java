package dev.ercan.auth.boilerplate.provider.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class InMemoryRateLimiterTest {

  private final InMemoryRateLimiter rateLimiter = new InMemoryRateLimiter();

  @Test
  void tryConsumeAllowsConfiguredCapacityThenRejectsFurtherRequests() {
    Duration window = Duration.ofDays(1);

    assertTrue(rateLimiter.tryConsume("client", 2, window));
    assertTrue(rateLimiter.tryConsume("client", 2, window));
    assertFalse(rateLimiter.tryConsume("client", 2, window));
  }

  @Test
  void keysHaveIndependentBuckets() {
    Duration window = Duration.ofDays(1);
    assertTrue(rateLimiter.tryConsume("client-a", 1, window));

    assertFalse(rateLimiter.tryConsume("client-a", 1, window));
    assertTrue(rateLimiter.tryConsume("client-b", 1, window));
  }

  @Test
  void rollbackRestoresOneConsumedToken() {
    Duration window = Duration.ofDays(1);
    assertTrue(rateLimiter.tryConsume("client", 1, window));
    assertFalse(rateLimiter.tryConsume("client", 1, window));

    rateLimiter.rollback("client", 1, window);

    assertTrue(rateLimiter.tryConsume("client", 1, window));
    assertFalse(rateLimiter.tryConsume("client", 1, window));
  }
}
