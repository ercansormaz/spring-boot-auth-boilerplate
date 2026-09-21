package dev.ercan.auth.boilerplate.facade;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationTokenFacade {

  private final DeviceService deviceService;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final LoginHistoryService loginHistoryService;

  @Value("${auth.token.access.duration}")
  private Duration accessTokenDuration;

  @Value("${auth.token.refresh.duration}")
  private Duration refreshTokenDuration;

  @Transactional
  public IssuedTokens completeAuthentication(Account account, AbstractAuthRequest request, AuthProviderType provider) {
    Device device = deviceService.registerOrUpdateDevice(account, request.getDevice());

    AccessToken accessToken = rotateAccessToken(device);

    RefreshToken refreshToken = null;
    if (request.isRemember() || AuthProviderType.REFRESH_TOKEN.equals(provider)) {
      refreshToken = rotateRefreshToken(device);
    } else {
      RefreshToken refreshTokenToDelete = refreshTokenService.getByDevice(device);
      if (Objects.nonNull(refreshTokenToDelete)) {
        refreshTokenService.delete(refreshTokenToDelete);
      }
    }

    loginHistoryService.recordLogin(device, provider);

    return new IssuedTokens(accessToken, refreshToken);
  }

  private AccessToken rotateAccessToken(Device device) {
    AccessToken accessToken = accessTokenService.getByDevice(device);

    if (Objects.isNull(accessToken)) {
      accessToken = new AccessToken();
      accessToken.setDevice(device);
    }

    accessToken.setSalt(UUID.randomUUID().toString());
    accessToken.setExpiresAt(Instant.now().plusSeconds(accessTokenDuration.toSeconds()));

    return accessTokenService.save(accessToken);
  }

  private RefreshToken rotateRefreshToken(Device device) {
    RefreshToken refreshToken = refreshTokenService.getByDevice(device);

    if (Objects.isNull(refreshToken)) {
      refreshToken = new RefreshToken();
      refreshToken.setDevice(device);
    }

    refreshToken.setSalt(UUID.randomUUID().toString());
    refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshTokenDuration.toSeconds()));

    return refreshTokenService.save(refreshToken);
  }
}
