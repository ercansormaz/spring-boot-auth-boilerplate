package dev.ercan.auth.boilerplate.provider.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.exception.UnsupportedStrategyException;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import java.util.List;
import org.junit.jupiter.api.Test;

class AuthProviderRegistryTest {

  @Test
  void returnsProviderRegisteredForType() {
    AuthProvider provider = providerWithType(AuthProviderType.ANONYMOUS);
    AuthProviderRegistry registry = new AuthProviderRegistry(List.of(provider));

    assertSame(provider, registry.getAuthProvider(AuthProviderType.ANONYMOUS));
  }

  @Test
  void throwsUnsupportedStrategyWhenNoProviderIsRegistered() {
    AuthProviderRegistry registry = new AuthProviderRegistry(List.of());

    UnsupportedStrategyException exception = assertThrows(
        UnsupportedStrategyException.class,
        () -> registry.getAuthProvider(AuthProviderType.ANONYMOUS));

    assertEquals(ErrorType.UNSUPPORTED_STRATEGY, exception.getErrorType());
  }

  private static AuthProvider providerWithType(AuthProviderType type) {
    AuthProvider provider = mock(AuthProvider.class);
    when(provider.getAuthProviderType()).thenReturn(type);
    return provider;
  }
}
