package dev.ercan.auth.boilerplate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class EmailNormalizerTest {

  @Test
  void normalizeTrimsAndLowercasesUsingRootLocale() {
    assertEquals("mixed@example.com", EmailNormalizer.normalize("  MiXeD@Example.COM  "));
  }

  @Test
  void normalizePreservesNullAndValuesWithoutText() {
    assertNull(EmailNormalizer.normalize(null));
    assertEquals("", EmailNormalizer.normalize(""));
    assertEquals(" \t ", EmailNormalizer.normalize(" \t "));
  }

  @Test
  void normalizeLeavesAlreadyNormalizedEmailUnchanged() {
    assertEquals("user@example.com", EmailNormalizer.normalize("user@example.com"));
  }
}
