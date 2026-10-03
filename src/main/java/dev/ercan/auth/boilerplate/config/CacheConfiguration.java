package dev.ercan.auth.boilerplate.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import java.time.Duration;

@EnableCaching
@Configuration
public class CacheConfiguration {

  public static final String EXPIRE_AFTER_WRITE = "expireAfterWrite";
  public static final String EXPIRE_AFTER_ACCESS = "expireAfterAccess";

  @Primary
  @Bean(EXPIRE_AFTER_WRITE)
  public CacheManager expireAfterWriteCacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheWriter redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
    RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(15));

    return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
  }

  @Bean(EXPIRE_AFTER_ACCESS)
  public CacheManager expireAfterAccessCacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheWriter redisCacheWriter = RedisCacheWriter.lockingRedisCacheWriter(connectionFactory);
    RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(15))
        .enableTimeToIdle();

    return new RedisCacheManager(redisCacheWriter, redisCacheConfiguration);
  }

}
