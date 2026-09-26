package dev.ercan.auth.boilerplate.model.pojo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthTokenDetailTest {

  @Test
  void buildFromAccessTokenCopiesTokenAndAccountDetails() {
    UUID deviceId = UUID.randomUUID();
    Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");
    AccessToken token = new AccessToken();
    token.setId(12L);
    token.setSalt("access-salt");
    token.setExpiresAt(expiresAt);
    token.setDevice(deviceWithId(deviceId));

    AuthTokenDetail detail = AuthTokenDetail.build(token, 34L);

    assertEquals(12L, detail.getId());
    assertEquals(34L, detail.getAccountId());
    assertEquals(deviceId, detail.getDeviceId());
    assertEquals("access-salt", detail.getSalt());
    assertEquals(Scope.ACCESS, detail.getScope());
    assertEquals(expiresAt, detail.getExpiresAt());
  }

  @Test
  void buildFromRefreshTokenUsesRefreshScopeAndCopiesTokenDetails() {
    UUID deviceId = UUID.randomUUID();
    Instant expiresAt = Instant.parse("2030-01-01T00:00:00Z");
    RefreshToken token = new RefreshToken();
    token.setId(56L);
    token.setSalt("refresh-salt");
    token.setExpiresAt(expiresAt);
    token.setDevice(deviceWithId(deviceId));

    AuthTokenDetail detail = AuthTokenDetail.build(token, 78L);

    assertEquals(56L, detail.getId());
    assertEquals(78L, detail.getAccountId());
    assertEquals(deviceId, detail.getDeviceId());
    assertEquals("refresh-salt", detail.getSalt());
    assertEquals(Scope.REFRESH, detail.getScope());
    assertEquals(expiresAt, detail.getExpiresAt());
    assertNotEquals(Scope.ACCESS, detail.getScope());
  }

  private static Device deviceWithId(UUID id) {
    Device device = new Device();
    device.setId(id);
    return device;
  }
}
