package dev.ercan.auth.boilerplate.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HMacUtil {

  private static final String ALGORITHM = "HmacSHA256";

  private HMacUtil() {}

  public static String hmac(String key, String data) throws NoSuchAlgorithmException, InvalidKeyException {
    SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    Mac mac = Mac.getInstance(ALGORITHM);
    mac.init(secretKeySpec);

    byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
  }

}