package dev.ercan.auth.boilerplate.config.property;

import dev.ercan.auth.boilerplate.model.pojo.RateLimitPolicy;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "otp.email")
public class EmailOtpProperties {

  Duration ttl;

  int length;

  int attemptCount;

  Duration retryDelay;

  RateLimitPolicy perEmailPolicy;

}
