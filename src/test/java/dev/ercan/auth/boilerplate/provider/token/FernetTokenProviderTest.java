package dev.ercan.auth.boilerplate.provider.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class FernetTokenProviderTest {

  private final String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(new byte[32]);
  private FernetTokenProvider provider;

  @BeforeEach
  void setUp() {
    provider = new FernetTokenProvider();
    ReflectionTestUtils.setField(provider, "secret", secret);
    provider.init();
  }

  @Test
  void encryptAndDecryptRoundTripTokenDetails() {
    AuthTokenDetail original = tokenDetail(12L, 34L, Scope.REFRESH, Instant.now().plusSeconds(300));

    AuthTokenDetail encrypted = provider.encrypt(original);
    AuthTokenDetail decrypted = provider.decrypt(encrypted.getRaw());

    assertEquals("Bearer", encrypted.getType());
    assertNotNull(encrypted.getRaw());
    assertEquals(original.getId(), decrypted.getId());
    assertEquals(original.getAccountId(), decrypted.getAccountId());
    assertEquals(original.getDeviceId(), decrypted.getDeviceId());
    assertEquals(original.getSalt(), decrypted.getSalt());
    assertEquals(original.getScope(), decrypted.getScope());
    assertEquals(original.getType(), decrypted.getType());
    assertEquals(original.getExpiresAt().getEpochSecond(), decrypted.getExpiresAt().getEpochSecond());
    assertEquals(encrypted.getRaw(), decrypted.getRaw());
  }

  @Test
  void decryptReturnsNullForMalformedAndExpiredTokens() {
    assertNull(provider.decrypt("malformed-token"));

    AuthTokenDetail expired = provider.encrypt(
        tokenDetail(1L, 2L, Scope.ACCESS, Instant.now().minusSeconds(300)));
    assertNull(provider.decrypt(expired.getRaw()));
  }

  @Test
  void initRejectsBlankSecret() {
    FernetTokenProvider invalidProvider = new FernetTokenProvider();
    ReflectionTestUtils.setField(invalidProvider, "secret", " ");

    assertThrows(IllegalStateException.class, invalidProvider::init);
  }

  private static AuthTokenDetail tokenDetail(Long id, Long accountId, Scope scope, Instant expiresAt) {
    AuthTokenDetail detail = new AuthTokenDetail();
    detail.setId(id);
    detail.setAccountId(accountId);
    detail.setDeviceId(UUID.fromString("2b1f36a8-9568-4bd0-8658-741bdcbbf771"));
    detail.setSalt("test-salt");
    detail.setScope(scope);
    detail.setExpiresAt(expiresAt);
    return detail;
  }
}
