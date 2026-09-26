package dev.ercan.auth.boilerplate.config.property;

import dev.ercan.auth.boilerplate.model.enums.RateLimitScope;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

  String ipHeaderName;

  List<String> trustedProxies = List.of();

  Map<RateLimitType, Map<RateLimitScope, Policy>> policies = new EnumMap<>(RateLimitType.class);

  @Getter
  @Setter
  public static class Policy {
    private int limit = 0;
    private Duration window;
    private boolean enabled;
  }

  public Policy getPolicyByTypeAndScope(RateLimitType type, RateLimitScope scope) {
    return policies
        .getOrDefault(type, Map.of())
        .get(scope);
  }
}
