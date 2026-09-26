package dev.ercan.auth.boilerplate.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.DeviceRequest;
import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.pojo.IssuedTokens;
import dev.ercan.auth.boilerplate.service.AccessTokenService;
import dev.ercan.auth.boilerplate.service.DeviceService;
import dev.ercan.auth.boilerplate.service.LoginHistoryService;
import dev.ercan.auth.boilerplate.service.RefreshTokenService;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AuthenticationTokenFacadeTest {

  @Test
  void completesAuthenticationAndRemovesRefreshTokenWhenRememberIsFalse() {
    DeviceService deviceService = mock(DeviceService.class);
    AccessTokenService accessTokenService = mock(AccessTokenService.class);
    RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    LoginHistoryService loginHistoryService = mock(LoginHistoryService.class);
    AuthenticationTokenFacade facade = facade(
        deviceService, accessTokenService, refreshTokenService, loginHistoryService);
    Account account = new Account();
    Device device = new Device();
    AccessToken oldAccessToken = new AccessToken();
    RefreshToken oldRefreshToken = new RefreshToken();
    AbstractAuthRequest request = request();
    when(deviceService.registerOrUpdateDevice(account, request.getDevice())).thenReturn(device);
    when(accessTokenService.getByDevice(device)).thenReturn(oldAccessToken);
    when(accessTokenService.save(oldAccessToken)).thenReturn(oldAccessToken);
    when(refreshTokenService.getByDevice(device)).thenReturn(oldRefreshToken);

    IssuedTokens issuedTokens = facade.completeAuthentication(
        account, request, AuthProviderType.EMAIL);

    assertEquals(oldAccessToken, issuedTokens.accessToken());
    assertNull(issuedTokens.refreshToken());
    assertNotNull(oldAccessToken.getSalt());
    assertTrueWithin(oldAccessToken.getExpiresAt(), Duration.ofMinutes(1));
    verify(refreshTokenService).delete(oldRefreshToken);
    verify(loginHistoryService).recordLogin(device, AuthProviderType.EMAIL);
    verify(refreshTokenService, never()).save(any(RefreshToken.class));
  }

  @Test
  void keepsRefreshTokenForRememberedAuthentication() {
    DeviceService deviceService = mock(DeviceService.class);
    AccessTokenService accessTokenService = mock(AccessTokenService.class);
    RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    LoginHistoryService loginHistoryService = mock(LoginHistoryService.class);
    AuthenticationTokenFacade facade = facade(
        deviceService, accessTokenService, refreshTokenService, loginHistoryService);
    Account account = new Account();
    Device device = new Device();
    AbstractAuthRequest request = request();
    when(deviceService.registerOrUpdateDevice(account, request.getDevice())).thenReturn(device);
    when(accessTokenService.getByDevice(device)).thenReturn(null);
    when(accessTokenService.save(any(AccessToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(refreshTokenService.getByDevice(device)).thenReturn(null);
    when(refreshTokenService.save(any(RefreshToken.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    request.setRemember(true);

    IssuedTokens issuedTokens = facade.completeAuthentication(
        account, request, AuthProviderType.EMAIL);

    assertNotNull(issuedTokens.refreshToken());
    assertNotNull(issuedTokens.accessToken());
    verify(refreshTokenService, never()).delete(any(RefreshToken.class));
  }

  private static AuthenticationTokenFacade facade(
      DeviceService deviceService,
      AccessTokenService accessTokenService,
      RefreshTokenService refreshTokenService,
      LoginHistoryService loginHistoryService) {
    AuthenticationTokenFacade facade = new AuthenticationTokenFacade(
        deviceService, accessTokenService, refreshTokenService, loginHistoryService);
    ReflectionTestUtils.setField(facade, "accessTokenDuration", Duration.ofMinutes(1));
    ReflectionTestUtils.setField(facade, "refreshTokenDuration", Duration.ofDays(10));
    return facade;
  }

  private static AbstractAuthRequest request() {
    AbstractAuthRequest request = new AbstractAuthRequest() {};
    request.setDevice(new DeviceRequest());
    return request;
  }

  private static void assertTrueWithin(Instant expiresAt, Duration duration) {
    Instant now = Instant.now();
    org.junit.jupiter.api.Assertions.assertTrue(expiresAt.isAfter(now));
    org.junit.jupiter.api.Assertions.assertTrue(expiresAt.isBefore(now.plus(duration).plusSeconds(2)));
  }
}
