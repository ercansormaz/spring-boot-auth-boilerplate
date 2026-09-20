package dev.ercan.auth.boilerplate.provider.ratelimit;

import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "ratelimit.storage-type", havingValue = "inmemory")
public class InMemoryRateLimiter implements RateLimiter {

  private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

  @Override
  public boolean tryConsume(String key, int limit, Duration window) {
    Bucket bucket = getBucket(key, limit, window);
    return bucket.tryConsume(1);
  }

  @Override
  public void rollback(String key, int limit, Duration window) {
    Bucket bucket = getBucket(key, limit, window);
    bucket.addTokens(1);
  }

  private Bucket getBucket(String key, int limit, Duration window) {
    return buckets.computeIfAbsent(key, k -> {
      Bandwidth limitRule = Bandwidth.builder()
          .capacity(limit)
          .refillGreedy(limit, window)
          .build();
      return Bucket.builder().addLimit(limitRule).build();
    });
  }
}
