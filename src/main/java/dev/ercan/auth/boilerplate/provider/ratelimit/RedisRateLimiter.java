package dev.ercan.auth.boilerplate.provider.ratelimit;

import dev.ercan.auth.boilerplate.model.pojo.RateLimitResult;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {

  private final ProxyManager<String> proxyManager;

  @Override
  public boolean tryConsume(String key, int limit, Duration window) {
    Bucket bucket = getBucket(key, limit, window);
    return bucket.tryConsume(1);
  }

  @Override
  public RateLimitResult tryConsumeAndGet(String key, int limit, Duration window) {
    Bucket bucket = getBucket(key, limit, window);
    ConsumptionProbe result = bucket.tryConsumeAndReturnRemaining(1);
    return new RateLimitResult(result.isConsumed(), result.getRemainingTokens(),
        Duration.ofNanos(result.getNanosToWaitForRefill()));
  }

  @Override
  public void rollback(String key, int limit, Duration window) {
    Bucket bucket = getBucket(key, limit, window);
    bucket.addTokens(1);
  }

  private Bucket getBucket(String key, int limit, Duration window) {
    return proxyManager.builder().build(key, () -> {
      Bandwidth limitRule = Bandwidth.builder().capacity(limit).refillGreedy(limit, window).build();
      return BucketConfiguration.builder().addLimit(limitRule).build();
    });
  }
}
