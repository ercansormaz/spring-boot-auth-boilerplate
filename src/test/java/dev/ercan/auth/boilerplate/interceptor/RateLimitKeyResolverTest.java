package dev.ercan.auth.boilerplate.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.annotation.RateLimit;
import dev.ercan.auth.boilerplate.config.property.RateLimitProperties;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.RateLimitScope;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class RateLimitKeyResolverTest {

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void resolvesGlobalScopeKeyWithoutRequestIdentity() throws Exception {
    RateLimitProperties properties = new RateLimitProperties();
    RateLimitKeyResolver resolver = new RateLimitKeyResolver(properties);
    RateLimit annotation = rateLimit(RateLimitScope.GLOBAL);

    assertEquals(
        "rl:EMAIL_OTP_REQUESTED:global",
        resolver.resolve(mock(HttpServletRequest.class), annotation));
  }

  @Test
  void resolvesUserScopeFromAuthenticatedAccount() throws Exception {
    RateLimitProperties properties = new RateLimitProperties();
    RateLimitKeyResolver resolver = new RateLimitKeyResolver(properties);
    Account account = new Account();
    account.setId(42L);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(account, null, List.of()));

    assertEquals(
        "rl:EMAIL_OTP_REQUESTED:42",
        resolver.resolve(mock(HttpServletRequest.class), rateLimit(RateLimitScope.USER)));
  }

  @Test
  void usesFirstUntrustedForwardedAddressBehindTrustedProxies() throws Exception {
    RateLimitProperties properties = new RateLimitProperties();
    properties.setIpHeaderName("X-Forwarded-For");
    properties.setTrustedProxies(List.of("10.0.0.0/8"));
    RateLimitKeyResolver resolver = new RateLimitKeyResolver(properties);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRemoteAddr()).thenReturn("10.0.0.3");
    when(request.getHeader("X-Forwarded-For"))
        .thenReturn("192.0.2.10, 10.0.0.2");

    assertEquals(
        "rl:EMAIL_OTP_REQUESTED:192.0.2.10",
        resolver.resolve(request, rateLimit(RateLimitScope.IP)));
  }

  @Test
  void rejectsUserScopeWhenNoAuthenticatedAccountExists() throws Exception {
    RateLimitKeyResolver resolver = new RateLimitKeyResolver(new RateLimitProperties());

    assertThrows(
        IllegalStateException.class,
        () -> resolver.resolve(
            mock(HttpServletRequest.class), rateLimit(RateLimitScope.USER)));
  }

  private static RateLimit rateLimit(RateLimitScope scope) throws Exception {
    Method method = RateLimitMethods.class.getDeclaredMethod("endpoint");
    RateLimit annotation = method.getAnnotation(RateLimit.class);
    return new RateLimit() {
      @Override
      public RateLimitType type() {
        return RateLimitType.EMAIL_OTP_REQUESTED;
      }

      @Override
      public RateLimitScope scope() {
        return scope;
      }

      @Override
      public org.springframework.http.HttpStatus[] statuses() {
        return annotation.statuses();
      }

      @Override
      public Class<? extends java.lang.annotation.Annotation> annotationType() {
        return RateLimit.class;
      }
    };
  }

  private static final class RateLimitMethods {

    @RateLimit(type = RateLimitType.EMAIL_OTP_REQUESTED, scope = RateLimitScope.GLOBAL, statuses = {})
    private void endpoint() {}
  }
}
