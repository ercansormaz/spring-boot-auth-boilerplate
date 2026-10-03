package dev.ercan.auth.boilerplate.provider.ratelimit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.model.pojo.RateLimitResult;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.BucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.distributed.proxy.RemoteBucketBuilder;
import java.time.Duration;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class RedisRateLimiterTest {

  @Test
  void tryConsumeDelegatesToBucket() {
    ProxyManager<String> proxyManager = mock(ProxyManager.class);
    RemoteBucketBuilder<String> builder = mock(RemoteBucketBuilder.class);
    BucketProxy bucket = mock(BucketProxy.class);
    when(proxyManager.builder()).thenReturn(builder);
    when(builder.build(eq("user:1"), any(Supplier.class))).thenReturn(bucket);
    when(bucket.tryConsume(1)).thenReturn(true);
    RedisRateLimiter limiter = new RedisRateLimiter(proxyManager);

    boolean consumed = limiter.tryConsume("user:1", 10, Duration.ofMinutes(1));

    assertTrue(consumed);
    verify(builder).build(eq("user:1"), any(Supplier.class));
  }

  @Test
  void tryConsumeAndGetReturnsDetailedResult() {
    ProxyManager<String> proxyManager = mock(ProxyManager.class);
    RemoteBucketBuilder<String> builder = mock(RemoteBucketBuilder.class);
    BucketProxy bucket = mock(BucketProxy.class);
    ConsumptionProbe probe = mock(ConsumptionProbe.class);
    when(proxyManager.builder()).thenReturn(builder);
    when(builder.build(eq("user:2"), any(Supplier.class))).thenReturn(bucket);
    when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
    when(probe.isConsumed()).thenReturn(true);
    when(probe.getRemainingTokens()).thenReturn(4L);
    when(probe.getNanosToWaitForRefill()).thenReturn(250_000_000L);
    RedisRateLimiter limiter = new RedisRateLimiter(proxyManager);

    RateLimitResult result = limiter.tryConsumeAndGet("user:2", 10, Duration.ofMinutes(1));

    assertEquals(true, result.consumed());
    assertEquals(4L, result.remainingTokens());
    assertEquals(Duration.ofNanos(250_000_000L), result.timeToWait());
  }

  @Test
  void rollbackAddsOneTokenBackToBucket() {
    ProxyManager<String> proxyManager = mock(ProxyManager.class);
    RemoteBucketBuilder<String> builder = mock(RemoteBucketBuilder.class);
    BucketProxy bucket = mock(BucketProxy.class);
    when(proxyManager.builder()).thenReturn(builder);
    when(builder.build(eq("user:3"), any(Supplier.class))).thenReturn(bucket);
    RedisRateLimiter limiter = new RedisRateLimiter(proxyManager);

    limiter.rollback("user:3", 10, Duration.ofMinutes(1));

    verify(bucket).addTokens(1);
  }
}
