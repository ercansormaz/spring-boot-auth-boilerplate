package dev.ercan.auth.boilerplate.facade;

import dev.ercan.auth.boilerplate.dto.response.ActiveSessionResponse;
import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.service.AccessTokenService;
import dev.ercan.auth.boilerplate.service.DeviceService;
import dev.ercan.auth.boilerplate.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionFacade {

  private final DeviceService deviceService;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;

  public List<ActiveSessionResponse> getActiveSessions(Account account, UUID currentDeviceId) {
    List<Device> activeDevices = deviceService.getActivesByAccount(account);

    List<ActiveSessionResponse> result = new ArrayList<>();

    Instant now = Instant.now();

    activeDevices.forEach(device -> {
      boolean isCurrent = device.getId().equals(currentDeviceId);

      RefreshToken refreshToken = refreshTokenService.getByDevice(device);
      if (refreshToken != null && now.isBefore(refreshToken.getExpiresAt())) {
        result.add(toDto(device, refreshToken.getCreatedAt(), isCurrent));
        return;
      }

      AccessToken accessToken = accessTokenService.getByDevice(device);
      if (accessToken != null && now.isBefore(accessToken.getExpiresAt())) {
        result.add(toDto(device, accessToken.getCreatedAt(), isCurrent));
      }
    });

    return result;
  }

  @Transactional
  public void logout(Account account, UUID deviceId) {
    Device device = deviceService.getByAccountAndId(account, deviceId);
    if (Objects.nonNull(device)) {
      logoutDevice(device);
    }
  }

  @Transactional
  public void logoutOthers(Account account, UUID currentDeviceId) {
    List<Device> devices = deviceService.getActivesByAccount(account);

    devices.stream()
        .filter(device -> !device.getId().equals(currentDeviceId))
        .forEach(this::logoutDevice);
  }

  @Transactional
  public void logoutAll(Account account) {
    List<Device> devices = deviceService.getActivesByAccount(account);
    devices.forEach(this::logoutDevice);
  }

  private void logoutDevice(Device device) {
    AccessToken accessToken = accessTokenService.getByDevice(device);
    if (Objects.nonNull(accessToken)) {
      accessTokenService.delete(accessToken);
    }

    RefreshToken refreshToken = refreshTokenService.getByDevice(device);
    if (Objects.nonNull(refreshToken)) {
      refreshTokenService.delete(refreshToken);
    }

    deviceService.deactivate(device);
  }

  private ActiveSessionResponse toDto(Device device, Instant createdAt, boolean isCurrent) {
    return new ActiveSessionResponse(device.getId().toString(), device.getPlatform(), device.getType(),
        device.getModel(), createdAt, isCurrent);
  }

}
