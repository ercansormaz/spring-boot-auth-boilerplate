package dev.ercan.auth.boilerplate.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

class CacheConfigurationTest {

  private final CacheConfiguration configuration = new CacheConfiguration();

  @Test
  void expireAfterWriteCacheManagerUsesRedisCacheManager() {
    RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

    CacheManager cacheManager = configuration.expireAfterWriteCacheManager(connectionFactory);

    assertInstanceOf(RedisCacheManager.class, cacheManager);
  }

  @Test
  void expireAfterAccessCacheManagerUsesRedisCacheManager() {
    RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

    CacheManager cacheManager = configuration.expireAfterAccessCacheManager(connectionFactory);

    assertInstanceOf(RedisCacheManager.class, cacheManager);
  }
}
