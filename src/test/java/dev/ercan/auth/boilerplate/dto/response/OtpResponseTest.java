package dev.ercan.auth.boilerplate.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class OtpResponseTest {

  @Test
  void constructsOtpResponseWithRetryDetailsWhenFlowIsRetryableAndQuotaRemains() {
    OtpDetail detail = new OtpDetail(
        "encoded-signature",
        "user@example.com",
        "123456",
        OtpFlowType.AUTHENTICATION,
        6,
        Duration.ofMinutes(3),
        2,
        Duration.ofSeconds(45)
    );

    OtpResponse response = new OtpResponse(detail);

    assertEquals("encoded-signature", response.getSignature());
    assertEquals(6, response.getLength());
    assertEquals(180, response.getExpiresIn());
    assertTrue(response.isRetryable());
    assertEquals(45L, response.getRetryIn());
  }

  @Test
  void omitsRetryDetailsWhenRemainingRetryCountIsZero() {
    OtpDetail detail = new OtpDetail(
        "encoded-signature",
        "user@example.com",
        "123456",
        OtpFlowType.AUTHENTICATION,
        6,
        Duration.ofMinutes(3),
        0,
        Duration.ofSeconds(45)
    );

    OtpResponse response = new OtpResponse(detail);

    assertEquals("encoded-signature", response.getSignature());
    assertEquals(6, response.getLength());
    assertEquals(180, response.getExpiresIn());
    assertFalse(response.isRetryable());
    assertNull(response.getRetryIn());
  }
}
