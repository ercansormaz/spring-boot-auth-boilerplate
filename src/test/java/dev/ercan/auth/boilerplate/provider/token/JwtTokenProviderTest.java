package dev.ercan.auth.boilerplate.provider.token;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail;
import dev.ercan.auth.boilerplate.model.pojo.AuthTokenDetail.Scope;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

  private static final String SECRET = "unit-test-secret-with-sufficient-length";
  private static final String ISSUER = "unit-test-issuer";

  private JwtTokenProvider provider;

  @BeforeEach
  void setUp() {
    provider = new JwtTokenProvider();
    ReflectionTestUtils.setField(provider, "secret", SECRET);
    ReflectionTestUtils.setField(provider, "issuer", ISSUER);
    provider.init();
  }

  @Test
  void encryptAndDecryptRoundTripTokenClaims() {
    AuthTokenDetail original = tokenDetail(12L, 34L, Scope.ACCESS, Instant.now().plusSeconds(300));

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
  }

  @Test
  void decryptReturnsNullForMalformedToken() {
    assertNull(provider.decrypt("not-a-jwt"));
  }

  @Test
  void decryptReturnsNullForTokenSignedByAnotherSecret() {
    AuthTokenDetail encrypted = provider.encrypt(
        tokenDetail(1L, 2L, Scope.REFRESH, Instant.now().plusSeconds(300)));
    JwtTokenProvider otherProvider = new JwtTokenProvider();
    ReflectionTestUtils.setField(otherProvider, "secret", "different-unit-test-secret");
    ReflectionTestUtils.setField(otherProvider, "issuer", ISSUER);
    otherProvider.init();

    assertNull(otherProvider.decrypt(encrypted.getRaw()));
  }

  @Test
  void initRejectsBlankSecret() {
    JwtTokenProvider invalidProvider = new JwtTokenProvider();
    ReflectionTestUtils.setField(invalidProvider, "secret", " ");
    ReflectionTestUtils.setField(invalidProvider, "issuer", ISSUER);

    org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, invalidProvider::init);
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
