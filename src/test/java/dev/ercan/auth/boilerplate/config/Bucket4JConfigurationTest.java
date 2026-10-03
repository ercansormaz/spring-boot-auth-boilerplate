package dev.ercan.auth.boilerplate.config;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.lettuce.core.AbstractRedisClient;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

class Bucket4JConfigurationTest {

  private final Bucket4JConfiguration configuration = new Bucket4JConfiguration();

  @Test
  void throwsExceptionWhenRedisNativeClientIsUnsupported() {
    LettuceConnectionFactory connectionFactory = mock(LettuceConnectionFactory.class);
    when(connectionFactory.getNativeClient()).thenReturn(mock(AbstractRedisClient.class));

    assertThrows(IllegalStateException.class, () -> configuration.proxyManager(connectionFactory));
  }
}
