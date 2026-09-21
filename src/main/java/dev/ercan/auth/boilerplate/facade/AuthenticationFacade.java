package dev.ercan.auth.boilerplate.facade;

import dev.ercan.auth.boilerplate.dto.request.AbstractAuthRequest;
import dev.ercan.auth.boilerplate.dto.response.AuthResponse;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.IssuedTokens;
import dev.ercan.auth.boilerplate.provider.auth.AuthProvider;
import dev.ercan.auth.boilerplate.provider.auth.AuthProviderRegistry;
import dev.ercan.auth.boilerplate.service.port.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationFacade {

  private final AuthProviderRegistry authProviderRegistry;
  private final AuthenticationTokenFacade authenticationTokenFacade;
  private final TokenProvider tokenProvider;

  public AuthResponse authenticate(AbstractAuthRequest authRequest, AuthProviderType authProviderType) {
    AuthProvider authProvider = authProviderRegistry.getAuthProvider(authProviderType);

    Account account = authProvider.resolveAccount(authRequest);

    IssuedTokens tokenResult = authenticationTokenFacade.completeAuthentication(account, authRequest, authProviderType);

    AuthTokenDetail accessToken = AuthTokenDetail.build(tokenResult.accessToken(), account.getId());
    accessToken = tokenProvider.encrypt(accessToken);

    AuthTokenDetail refreshToken = null;
    if (Objects.nonNull(tokenResult.refreshToken())) {
      refreshToken = AuthTokenDetail.build(tokenResult.refreshToken(), account.getId());
      refreshToken = tokenProvider.encrypt(refreshToken);
    }

    return buildAuthResponse(accessToken, refreshToken);
  }

  private AuthResponse buildAuthResponse(AuthTokenDetail accessToken, AuthTokenDetail refreshToken) {
    AuthResponse response = new AuthResponse();
    response.setAccessToken(accessToken.getRaw());
    response.setExpiresIn(calculateExpiresIn(accessToken.getExpiresAt()));
    response.setTokenType(accessToken.getType());

    if (Objects.nonNull(refreshToken)) {
      response.setRefreshToken(refreshToken.getRaw());
    }

    return response;
  }

  private Long calculateExpiresIn(Instant expireAt) {
    return Math.max(0, expireAt.getEpochSecond() - Instant.now().getEpochSecond());
  }
}
