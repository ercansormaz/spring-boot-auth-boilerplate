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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationTokenFacade {

  private final DeviceService deviceService;
  private final AccessTokenService accessTokenService;
  private final RefreshTokenService refreshTokenService;
  private final LoginHistoryService loginHistoryService;

  @Transactional
  public IssuedTokens completeAuthentication(Account account, AbstractAuthRequest request,
      AuthProviderType authProviderType) {
    Device device = deviceService.registerOrUpdateDevice(account, request.getDevice());

    AccessToken accessToken = accessTokenService.rotate(device);

    RefreshToken refreshToken = null;
    if (request.isRemember() || AuthProviderType.REFRESH_TOKEN.equals(authProviderType)) {
      refreshToken = refreshTokenService.rotate(device);
    } else {
      refreshTokenService.deleteByDevice(device);
    }

    loginHistoryService.recordLogin(device, authProviderType);

    return new IssuedTokens(accessToken, refreshToken);
  }

}
