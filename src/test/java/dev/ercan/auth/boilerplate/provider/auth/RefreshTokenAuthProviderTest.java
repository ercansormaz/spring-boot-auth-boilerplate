package dev.ercan.auth.boilerplate.provider.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.dto.request.DeviceRequest;
import dev.ercan.auth.boilerplate.dto.request.RefreshTokenAuthRequest;
import dev.ercan.auth.boilerplate.exception.InvalidCredentialsException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import dev.ercan.auth.boilerplate.service.AccountService;
import dev.ercan.auth.boilerplate.service.DeviceService;
import dev.ercan.auth.boilerplate.service.RefreshTokenService;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenAuthProviderTest {

  @Test
  void returnsAccountForValidRefreshTokenAndMatchingDevice() {
    TokenProvider tokenProvider = mock(TokenProvider.class);
    RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    DeviceService deviceService = mock(DeviceService.class);
    AccountService accountService = mock(AccountService.class);
    RefreshTokenAuthProvider provider = new RefreshTokenAuthProvider(
        tokenProvider, refreshTokenService, deviceService, accountService);
    UUID deviceId = UUID.randomUUID();
    AuthTokenDetail detail = tokenDetail(10L, 20L, deviceId, Scope.REFRESH, "salt");
    Device device = new Device();
    device.setIdentifier("device-1");
    RefreshToken storedToken = new RefreshToken();
    storedToken.setSalt("salt");
    Account account = new Account();
    RefreshTokenAuthRequest request = request("raw-token", "device-1");
    when(tokenProvider.decrypt("raw-token")).thenReturn(detail);
    when(refreshTokenService.getById(10L)).thenReturn(storedToken);
    when(deviceService.getById(deviceId)).thenReturn(device);
    when(accountService.getById(20L)).thenReturn(account);

    assertSame(account, provider.resolveAccount(request));
  }

  @Test
  void rejectsAccessTokenScopeAsInvalidRefreshToken() {
    TokenProvider tokenProvider = mock(TokenProvider.class);
    RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    DeviceService deviceService = mock(DeviceService.class);
    AccountService accountService = mock(AccountService.class);
    RefreshTokenAuthProvider provider = new RefreshTokenAuthProvider(
        tokenProvider, refreshTokenService, deviceService, accountService);
    when(tokenProvider.decrypt("raw-token"))
        .thenReturn(tokenDetail(10L, 20L, UUID.randomUUID(), Scope.ACCESS, "salt"));

    InvalidCredentialsException exception = assertThrows(
        InvalidCredentialsException.class,
        () -> provider.resolveAccount(request("raw-token", "device-1")));

    assertEquals(ErrorType.INVALID_REFRESH_TOKEN, exception.getErrorType());
    verify(refreshTokenService, never()).getById(10L);
  }

  private static RefreshTokenAuthRequest request(String token, String deviceIdentifier) {
    DeviceRequest device = new DeviceRequest();
    device.setIdentifier(deviceIdentifier);
    RefreshTokenAuthRequest request = new RefreshTokenAuthRequest();
    request.setRefreshToken(token);
    request.setDevice(device);
    return request;
  }

  private static AuthTokenDetail tokenDetail(
      Long id, Long accountId, UUID deviceId, Scope scope, String salt) {
    AuthTokenDetail detail = new AuthTokenDetail();
    detail.setId(id);
    detail.setAccountId(accountId);
    detail.setDeviceId(deviceId);
    detail.setScope(scope);
    detail.setSalt(salt);
    return detail;
  }
}
