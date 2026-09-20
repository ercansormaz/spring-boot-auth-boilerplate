package dev.ercan.auth.boilerplate.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomUtil {

  private static final String UPPERCASES = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERCASES = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUMBERS = "0123456789";
  private static final Random RANDOM = new SecureRandom();

  private RandomUtil() {
    /* This utility class should not be instantiated */
  }

  public static String lower(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(LOWERCASES.charAt(RANDOM.nextInt(LOWERCASES.length())));
    }
    return sb.toString();
  }

  public static String upper(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(UPPERCASES.charAt(RANDOM.nextInt(UPPERCASES.length())));
    }
    return sb.toString();
  }

  public static String number(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(NUMBERS.charAt(RANDOM.nextInt(NUMBERS.length())));
    }
    return sb.toString();
  }

}
