package dev.ercan.auth.boilerplate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;

class HMacUtilTest {

  @Test
  void hmacMatchesKnownSha256UrlSafeUnpaddedVector() throws NoSuchAlgorithmException, InvalidKeyException {
    String signature = HMacUtil.hmac("key", "The quick brown fox jumps over the lazy dog");

    assertEquals("97yD9DBThCSxMpjmqm-xQ-9NWaFJRhdZl0edvC0aPNg", signature);
    assertFalse(signature.contains("="));
  }

  @Test
  void hmacIsDeterministicAndDependsOnKeyAndData() throws NoSuchAlgorithmException, InvalidKeyException {
    String signature = HMacUtil.hmac("secret", "message");

    assertEquals(signature, HMacUtil.hmac("secret", "message"));
    assertNotEquals(signature, HMacUtil.hmac("other-secret", "message"));
    assertNotEquals(signature, HMacUtil.hmac("secret", "other-message"));
  }
}
