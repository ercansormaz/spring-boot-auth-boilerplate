package dev.ercan.auth.boilerplate.provider.auth;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.dto.request.RefreshTokenAuthRequest;
import dev.ercan.auth.boilerplate.exception.InvalidCredentialsException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import dev.ercan.auth.boilerplate.service.AccountService;
import dev.ercan.auth.boilerplate.service.DeviceService;
import dev.ercan.auth.boilerplate.service.RefreshTokenService;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenAuthProvider implements AuthProvider {

  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;
  private final DeviceService deviceService;
  private final AccountService accountService;

  @Override
  public Account resolveAccount(AbstractAuthRequest request) {
    RefreshTokenAuthRequest refreshTokenAuthRequest = (RefreshTokenAuthRequest) request;

    AuthTokenDetail authTokenDetail = tokenProvider.decrypt(refreshTokenAuthRequest.getRefreshToken());

    if (Objects.isNull(authTokenDetail)) {
      log.info("[REFRESH_TOKEN_AUTH_STRATEGY] [GET_ACCOUNT] [FAILED] [INVALID_TOKEN]");
      throw new InvalidCredentialsException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    if (!Scope.REFRESH.equals(authTokenDetail.getScope())) {
      log.info("[REFRESH_TOKEN_AUTH_STRATEGY] [GET_ACCOUNT] [FAILED] [IS_NOT_REFRESH_TOKEN]");
      throw new InvalidCredentialsException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    RefreshToken refreshToken = refreshTokenService.getById(authTokenDetail.getId());
    if (Objects.isNull(refreshToken) || !authTokenDetail.getSalt().equals(refreshToken.getSalt())) {
      log.info("[REFRESH_TOKEN_AUTH_STRATEGY] [GET_ACCOUNT] [FAILED] [TOKEN_REVOKED]");
      throw new InvalidCredentialsException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    Device device = deviceService.getById(authTokenDetail.getDeviceId());
    if (Objects.isNull(device)) {
      log.info("[REFRESH_TOKEN_AUTH_STRATEGY] [GET_ACCOUNT] [FAILED] [DEVICE_NOT_FOUND]");
      throw new InvalidCredentialsException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    if (!request.getDevice().getIdentifier().equals(device.getIdentifier())) {
      log.info("[REFRESH_TOKEN_AUTH_STRATEGY] [GET_ACCOUNT] [FAILED] [DEVICE_NOT_MATCH]");
      throw new InvalidCredentialsException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    Account account = accountService.getById(authTokenDetail.getAccountId());
    if (Objects.isNull(account)) {
      log.info("[REFRESH_TOKEN_AUTH_STRATEGY] [GET_ACCOUNT] [FAILED] [ACCOUNT_NOT_FOUND]");
      throw new InvalidCredentialsException(ErrorType.INVALID_REFRESH_TOKEN);
    }

    log.debug("[REFRESH_TOKEN_AUTH_STRATEGY] [GET_ACCOUNT] [SUCCESS] [ACCOUNT_ID={}]", account.getId());

    return account;
  }

  @Override
  public AuthProviderType getAuthProviderType() {
    return AuthProviderType.REFRESH_TOKEN;
  }
}
