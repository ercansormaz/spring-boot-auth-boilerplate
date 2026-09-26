package dev.ercan.auth.boilerplate.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.time.Duration;

@EnableCaching
@Configuration
@ConditionalOnProperty(name = "cache.provider", havingValue = "caffeine")
public class CaffeineCacheConfig {

  public static final String EXPIRE_AFTER_WRITE = "expireAfterWrite";
  public static final String EXPIRE_AFTER_ACCESS = "expireAfterAccess";

  @Primary
  @Bean(EXPIRE_AFTER_WRITE)
  public CacheManager expireAfterWriteCacheManager() {
    Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(15));
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(caffeine);
    return caffeineCacheManager;
  }

  @Bean(EXPIRE_AFTER_ACCESS)
  public CacheManager expireAfterAccessCacheManager() {
    Caffeine<Object, Object> caffeine = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(15));
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(caffeine);
    return caffeineCacheManager;
  }

}
