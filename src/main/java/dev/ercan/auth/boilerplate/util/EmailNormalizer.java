package dev.ercan.auth.boilerplate.util;

import org.springframework.util.StringUtils;
import java.util.Locale;

public final class EmailNormalizer {

  private EmailNormalizer() {
    /* This utility class should not be instantiated */
  }

  public static String normalize(String email) {
    if (!StringUtils.hasText(email)) {
      return email;
    }

    return email.trim().toLowerCase(Locale.ROOT);
  }
}
