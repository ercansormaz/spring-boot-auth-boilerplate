package dev.ercan.auth.boilerplate.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class RandomUtilTest {

  @Test
  void lowerReturnsRequestedLengthAndOnlyLowercaseLetters() {
    assertGeneratedValues(RandomUtil.lower(128), 128, "[a-z]+");
  }

  @Test
  void upperReturnsRequestedLengthAndOnlyUppercaseLetters() {
    assertGeneratedValues(RandomUtil.upper(128), 128, "[A-Z]+");
  }

  @Test
  void numberReturnsRequestedLengthAndOnlyDigits() {
    assertGeneratedValues(RandomUtil.number(128), 128, "[0-9]+");
  }

  @Test
  void generatorsReturnEmptyStringForZeroLength() {
    assertEquals("", RandomUtil.lower(0));
    assertEquals("", RandomUtil.upper(0));
    assertEquals("", RandomUtil.number(0));
  }

  private static void assertGeneratedValues(String value, int length, String pattern) {
    assertEquals(length, value.length());
    assertTrue(value.matches(pattern));
  }
}
