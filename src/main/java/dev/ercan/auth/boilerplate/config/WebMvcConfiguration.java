package dev.ercan.auth.boilerplate.config;

import dev.ercan.auth.boilerplate.interceptor.RateLimitInterceptor;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfiguration implements WebMvcConfigurer {

  private final RateLimitProperties rateLimitProperties;
  private final RateLimiter rateLimiter;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new RateLimitInterceptor(rateLimitProperties, rateLimiter));
  }

}