package dev.ercan.auth.boilerplate.interceptor;

import dev.ercan.auth.boilerplate.annotation.RateLimit;
import dev.ercan.auth.boilerplate.config.property.RateLimitProperties;
import dev.ercan.auth.boilerplate.config.property.RateLimitProperties.Policy;
import dev.ercan.auth.boilerplate.exception.RateLimitExceedException;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

  private static final String CONSUMED_LIMITS_ATTRIBUTE = RateLimitInterceptor.class.getName() + ".consumedLimits";

  private final RateLimitProperties rateLimitProperties;
  private final RateLimiter rateLimiter;
  private final RateLimitKeyResolver keyResolver;

  public RateLimitInterceptor(RateLimitProperties rateLimitProperties, RateLimiter rateLimiter) {
    this.rateLimitProperties = rateLimitProperties;
    this.rateLimiter = rateLimiter;
    this.keyResolver = new RateLimitKeyResolver(rateLimitProperties);
  }

  @Override
  public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull Object handler) {
    if (!(handler instanceof HandlerMethod method)) {
      return true;
    }

    List<ConsumedLimit> consumedLimits = new ArrayList<>();
    request.setAttribute(CONSUMED_LIMITS_ATTRIBUTE, consumedLimits);

    try {
      for (RateLimit rateLimit : method.getMethod().getAnnotationsByType(RateLimit.class)) {
        Policy policy = rateLimitProperties.getPolicyByTypeAndScope(rateLimit.type(), rateLimit.scope());

        if (policy == null || !policy.isEnabled()) {
          continue;
        }

        try {
          consume(request, rateLimit, policy, consumedLimits);
        } catch (IllegalStateException e) {
          rollback(consumedLimits);

          log.warn("[RATE_LIMIT_INTERCEPTOR] [FAILED] [MSG={}]", e.getMessage());
          throw new RateLimitExceedException(rateLimit.type().getErrorType());
        }
      }

      return true;
    } catch (RuntimeException e) {
      rollback(consumedLimits);
      throw e;
    }
  }

  @Override
  public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
      @NonNull Object handler, @Nullable Exception ex) {
    Object attribute = request.getAttribute(CONSUMED_LIMITS_ATTRIBUTE);

    if (!(attribute instanceof List<?> values)) {
      return;
    }

    for (Object value : values) {
      if (!(value instanceof ConsumedLimit(String key, int limit, Duration window, HttpStatus[] countedStatuses))) {
        continue;
      }

      if (!shouldCountRequest(response.getStatus(), countedStatuses)) {
        rateLimiter.rollback(key, limit, window);
      }
    }

    values.clear();
  }

  private void rollback(List<ConsumedLimit> consumedLimits) {
    for (ConsumedLimit consumedLimit : consumedLimits) {
      rateLimiter.rollback(consumedLimit.key(), consumedLimit.limit(), consumedLimit.window());
    }

    consumedLimits.clear();
  }

  private void consume(HttpServletRequest request, RateLimit rateLimit, Policy policy,
      List<ConsumedLimit> consumedLimits) {
    String key = keyResolver.resolve(request, rateLimit);

    if (!rateLimiter.tryConsume(key, policy.getLimit(), policy.getWindow())) {
      rollback(consumedLimits);
      throw new RateLimitExceedException(rateLimit.type().getErrorType());
    }

    consumedLimits.add(new ConsumedLimit(key, policy.getLimit(), policy.getWindow(), rateLimit.statuses()));
  }

  private boolean shouldCountRequest(int currentStatusCode, HttpStatus[] configuredStatuses) {
    if (configuredStatuses == null || configuredStatuses.length == 0) {
      return true;
    }
    for (HttpStatus status : configuredStatuses) {
      if (status.value() == currentStatusCode) {
        return true;
      }
    }
    return false;
  }

  private record ConsumedLimit(String key, int limit, Duration window, HttpStatus[] countedStatuses) {

  }
}
