package dev.ercan.auth.boilerplate.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.IssuedTokens;
import dev.ercan.auth.boilerplate.provider.auth.AuthProvider;
import dev.ercan.auth.boilerplate.provider.auth.AuthProviderRegistry;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthenticationFacadeTest {

  @Test
  void authenticatesProviderAndBuildsResponseFromEncryptedAccessToken() {
    AuthProviderRegistry registry = mock(AuthProviderRegistry.class);
    AuthenticationTokenFacade tokenFacade = mock(AuthenticationTokenFacade.class);
    TokenProvider tokenProvider = mock(TokenProvider.class);
    AuthProvider provider = mock(AuthProvider.class);
    AuthenticationFacade facade = new AuthenticationFacade(registry, tokenFacade, tokenProvider);
    AbstractAuthRequest request = new AbstractAuthRequest() {};
    Account account = new Account();
    account.setId(20L);
    AccessToken accessToken = new AccessToken();
    accessToken.setId(10L);
    accessToken.setSalt("access-salt");
    accessToken.setExpiresAt(Instant.now().plusSeconds(60));
    Device device = new Device();
    device.setId(UUID.randomUUID());
    accessToken.setDevice(device);
    when(registry.getAuthProvider(AuthProviderType.EMAIL)).thenReturn(provider);
    when(provider.resolveAccount(request)).thenReturn(account);
    when(tokenFacade.completeAuthentication(account, request, AuthProviderType.EMAIL))
        .thenReturn(new IssuedTokens(accessToken, null));
    when(tokenProvider.encrypt(any(AuthTokenDetail.class))).thenAnswer(invocation -> {
      AuthTokenDetail detail = invocation.getArgument(0);
      detail.setRaw("encrypted-access");
      detail.setType("Bearer");
      return detail;
    });

    AuthResponse response = facade.authenticate(request, AuthProviderType.EMAIL);

    assertEquals("encrypted-access", response.getAccessToken());
    assertEquals("Bearer", response.getTokenType());
    assertTrue(response.getExpiresIn() >= 0);
    assertTrue(response.getExpiresIn() <= 60);
    assertNull(response.getRefreshToken());
    verify(provider).resolveAccount(request);
  }
}
