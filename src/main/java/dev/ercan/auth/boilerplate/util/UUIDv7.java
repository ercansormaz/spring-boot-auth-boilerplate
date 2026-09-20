package dev.ercan.auth.boilerplate.util;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

public class UUIDv7 {

  private static final Random random = new SecureRandom();
  private static long lastTimestamp = -1L;
  private static short sequence = 0;

  private UUIDv7() {
    /* This utility class should not be instantiated */
  }

  public static synchronized UUID randomUUID() {
    long currentTimestamp = System.currentTimeMillis();

    if (currentTimestamp < lastTimestamp) {
      // Clock moved backwards handling
      currentTimestamp = lastTimestamp;
    }

    if (currentTimestamp == lastTimestamp) {
      sequence = (short) ((sequence + 1) & 0x0FFF); // 12-bit counter
      if (sequence == 0) {
        // Counter overflowed within the same ms, wait for next ms
        while (currentTimestamp <= lastTimestamp) {
          currentTimestamp = System.currentTimeMillis();
        }
      }
    } else {
      sequence = (short) random.nextInt(0x1000); // Random initial sequence
      lastTimestamp = currentTimestamp;
    }

    // 48-bit timestamp
    long msb = (currentTimestamp & 0xFFFFFFFFFFFFL) << 16;
    // 4-bit version (7) + 12-bit sequence
    msb |= (0x7L << 12) | (sequence & 0x0FFF);

    // 2-bit variant (10xx) + 62-bit random data
    long lsb = (0x2L << 62) | (random.nextLong() & 0x3FFFFFFFFFFFFFFFL);

    return new UUID(msb, lsb);
  }
}
