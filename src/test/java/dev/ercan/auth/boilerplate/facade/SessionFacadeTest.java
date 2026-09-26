package dev.ercan.auth.boilerplate.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.dto.response.ActiveSessionResponse;
import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.model.enums.DevicePlatform;
import dev.ercan.auth.boilerplate.model.enums.DeviceType;
import dev.ercan.auth.boilerplate.service.AccessTokenService;
import dev.ercan.auth.boilerplate.service.DeviceService;
import dev.ercan.auth.boilerplate.service.RefreshTokenService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionFacadeTest {

  @Test
  void listsCurrentSessionUsingAccessTokenWhenNoRefreshTokenExists() {
    DeviceService deviceService = mock(DeviceService.class);
    AccessTokenService accessTokenService = mock(AccessTokenService.class);
    RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    SessionFacade facade = new SessionFacade(deviceService, accessTokenService, refreshTokenService);
    Account account = new Account();
    UUID currentId = UUID.randomUUID();
    Device device = device(currentId);
    AccessToken accessToken = new AccessToken();
    accessToken.setCreatedAt(Instant.now().minusSeconds(20));
    accessToken.setExpiresAt(Instant.now().plusSeconds(300));
    when(deviceService.getActivesByAccount(account)).thenReturn(List.of(device));
    when(refreshTokenService.getByDevice(device)).thenReturn(null);
    when(accessTokenService.getByDevice(device)).thenReturn(accessToken);

    List<ActiveSessionResponse> sessions = facade.getActiveSessions(account, currentId);

    assertEquals(1, sessions.size());
    assertTrue(sessions.getFirst().current());
    assertEquals(currentId.toString(), sessions.getFirst().deviceId());
    assertEquals(accessToken.getCreatedAt(), sessions.getFirst().createdAt());
  }

  @Test
  void logoutOthersPreservesCurrentDeviceAndRemovesOtherTokens() {
    DeviceService deviceService = mock(DeviceService.class);
    AccessTokenService accessTokenService = mock(AccessTokenService.class);
    RefreshTokenService refreshTokenService = mock(RefreshTokenService.class);
    SessionFacade facade = new SessionFacade(deviceService, accessTokenService, refreshTokenService);
    Account account = new Account();
    Device current = device(UUID.randomUUID());
    Device other = device(UUID.randomUUID());
    AccessToken accessToken = new AccessToken();
    RefreshToken refreshToken = new RefreshToken();
    when(deviceService.getActivesByAccount(account)).thenReturn(List.of(current, other));
    when(accessTokenService.getByDevice(other)).thenReturn(accessToken);
    when(refreshTokenService.getByDevice(other)).thenReturn(refreshToken);

    facade.logoutOthers(account, current.getId());

    verify(deviceService).deactivate(other);
    verify(deviceService, never()).deactivate(current);
    verify(accessTokenService).delete(accessToken);
    verify(refreshTokenService).delete(refreshToken);
    assertTrue(current.isActive());
  }

  private static Device device(UUID id) {
    Device device = new Device();
    device.setId(id);
    device.setPlatform(DevicePlatform.IOS);
    device.setType(DeviceType.PHONE);
    device.setModel("Phone");
    device.setActive(true);
    return device;
  }
}
