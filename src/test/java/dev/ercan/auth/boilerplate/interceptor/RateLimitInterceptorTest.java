package dev.ercan.auth.boilerplate.interceptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.annotation.RateLimit;
import dev.ercan.auth.boilerplate.config.property.RateLimitProperties;
import dev.ercan.auth.boilerplate.config.property.RateLimitProperties.Policy;
import dev.ercan.auth.boilerplate.model.enums.RateLimitScope;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

class RateLimitInterceptorTest {

  @Test
  void returnsPreviouslyConsumedTokenWhenResponseStatusIsNotCounted() throws Exception {
    Duration window = Duration.ofMinutes(1);
    RateLimitProperties properties = properties(window);
    RateLimiter rateLimiter = mock(RateLimiter.class);
    when(rateLimiter.tryConsume("rl:EMAIL_OTP_REQUESTED:global", 2, window)).thenReturn(true);
    RateLimitInterceptor interceptor = new RateLimitInterceptor(properties, rateLimiter);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    HandlerMethod handler = handler();

    assertTrue(interceptor.preHandle(request, response, handler));
    response.setStatus(HttpStatus.BAD_REQUEST.value());

    interceptor.afterCompletion(request, response, handler, null);

    verify(rateLimiter).rollback("rl:EMAIL_OTP_REQUESTED:global", 2, window);
  }

  private static RateLimitProperties properties(Duration window) {
    Policy policy = new Policy();
    policy.setLimit(2);
    policy.setWindow(window);
    Map<RateLimitScope, Policy> scopes = new EnumMap<>(RateLimitScope.class);
    scopes.put(RateLimitScope.GLOBAL, policy);
    Map<RateLimitType, Map<RateLimitScope, Policy>> policies = new EnumMap<>(RateLimitType.class);
    policies.put(RateLimitType.EMAIL_OTP_REQUESTED, scopes);
    RateLimitProperties properties = new RateLimitProperties();
    properties.setPolicies(policies);
    return properties;
  }

  private static HandlerMethod handler() throws Exception {
    Method method = TestController.class.getDeclaredMethod("endpoint");
    return new HandlerMethod(new TestController(), method);
  }

  private static final class TestController {

    @RateLimit(
        type = RateLimitType.EMAIL_OTP_REQUESTED,
        scope = RateLimitScope.GLOBAL,
        statuses = {HttpStatus.OK})
    private void endpoint() {}
  }
}
