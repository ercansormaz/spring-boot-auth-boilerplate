package dev.ercan.auth.boilerplate.model.pojo;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AuthTokenDetail {

  private Long id;
  private Long accountId;
  private UUID deviceId;
  private String salt;
  private Scope scope;
  private Instant expiresAt;
  private String type;
  private String raw;

  public enum Scope {
    ACCESS,
    REFRESH
  }

  public static AuthTokenDetail build(AccessToken accessToken, Long accountId) {
    AuthTokenDetail authTokenDetail = new AuthTokenDetail();
    authTokenDetail.id = accessToken.getId();
    authTokenDetail.accountId = accountId;
    authTokenDetail.deviceId = accessToken.getDevice().getId();
    authTokenDetail.salt = accessToken.getSalt();
    authTokenDetail.scope = Scope.ACCESS;
    authTokenDetail.expiresAt = accessToken.getExpiresAt();
    return  authTokenDetail;
  }

  public static AuthTokenDetail build(RefreshToken refreshToken, Long accountId) {
    AuthTokenDetail authTokenDetail = new AuthTokenDetail();
    authTokenDetail.id = refreshToken.getId();
    authTokenDetail.accountId = accountId;
    authTokenDetail.deviceId = refreshToken.getDevice().getId();
    authTokenDetail.salt = refreshToken.getSalt();
    authTokenDetail.scope = Scope.REFRESH;
    authTokenDetail.expiresAt = refreshToken.getExpiresAt();
    return  authTokenDetail;
  }
}