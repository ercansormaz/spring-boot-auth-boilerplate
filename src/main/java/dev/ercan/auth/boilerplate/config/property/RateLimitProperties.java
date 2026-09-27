package dev.ercan.auth.boilerplate.config.property;

import dev.ercan.auth.boilerplate.exception.MissingConfigurationException;
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
import java.util.Optional;

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
  }

  public Policy getPolicyByTypeAndScope(RateLimitType type, RateLimitScope scope) {
    return Optional.ofNullable(policies.get(type))
        .map(map -> map.get(scope))
        .orElseThrow(() -> new MissingConfigurationException("Rate limit policy not found for type " + type + " and scope " + scope));
  }
}
