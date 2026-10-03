package dev.ercan.auth.boilerplate.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce.LettuceBasedProxyManagerBuilder;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import java.time.Duration;

@Configuration
public class Bucket4JConfiguration {

  @Bean
  public ProxyManager<String> proxyManager(RedisConnectionFactory redisConnectionFactory) {
    LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) redisConnectionFactory;

    Object nativeClient = lettuceFactory.getNativeClient();

    LettuceBasedProxyManagerBuilder<String> proxyManagerBuilder = switch (nativeClient) {
      case RedisClusterClient clusterClient -> {
        StatefulRedisClusterConnection<String, byte[]> connection = clusterClient.connect(
            RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        yield Bucket4jLettuce.casBasedBuilder(connection);
      }
      case RedisClient redisClient -> {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
            RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));
        yield Bucket4jLettuce.casBasedBuilder(connection);
      }
      default -> throw new IllegalStateException("Unsupported redis configuration");
    };

    return proxyManagerBuilder
        .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1L)))
        .build();
  }

}
