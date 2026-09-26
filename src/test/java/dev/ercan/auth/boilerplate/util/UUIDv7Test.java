package dev.ercan.auth.boilerplate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UUIDv7Test {

  @Test
  void randomUUIDUsesVersionSevenAndRfcVariant() {
    UUID uuid = UUIDv7.randomUUID();

    assertEquals(7, uuid.version());
    assertEquals(2, uuid.variant());
  }

  @Test
  void successiveUuidsAreUniqueAndNondecreasingByTimestampAndSequence() {
    Set<UUID> generated = new HashSet<>();
    UUID previous = UUIDv7.randomUUID();
    generated.add(previous);

    for (int i = 0; i < 100; i++) {
      UUID current = UUIDv7.randomUUID();
      assertTrue(Long.compareUnsigned(current.getMostSignificantBits(), previous.getMostSignificantBits()) >= 0);
      generated.add(current);
      previous = current;
    }

    assertEquals(101, generated.size());
  }
}
