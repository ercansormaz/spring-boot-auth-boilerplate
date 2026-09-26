package dev.ercan.auth.boilerplate.facade;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.config.property.EmailOtpProperties;
import dev.ercan.auth.boilerplate.config.property.RateLimitProperties;
import dev.ercan.auth.boilerplate.dto.request.OtpRequest;
import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.service.OtpService;
import dev.ercan.auth.boilerplate.service.port.EmailSender;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import dev.ercan.auth.boilerplate.util.HMacUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class EmailOtpFacadeTest {

  private static final String ENCRYPTION_KEY = "otp-test-key";

  @Test
  void validateDeletesOtpWhenSignatureMatches() throws Exception {
    OtpService otpService = mock(OtpService.class);
    EmailOtpFacade facade = facade(otpService);
    Otp otp = otp(OtpFlowType.AUTHENTICATION, "user@example.com");
    when(otpService.getById(otp.getId())).thenReturn(otp);
    OtpRequest request = otpRequest(otp, "123456");

    assertTrue(facade.validate(request, OtpFlowType.AUTHENTICATION, "user@example.com"));

    verify(otpService).delete(otp);
    verify(otpService, never()).incrementAttempt(otp);
  }

  @Test
  void validateIncrementsAttemptsForIncorrectSignature() throws Exception {
    OtpService otpService = mock(OtpService.class);
    EmailOtpFacade facade = facade(otpService);
    Otp otp = otp(OtpFlowType.AUTHENTICATION, "user@example.com");
    when(otpService.getById(otp.getId())).thenReturn(otp);
    OtpRequest request = otpRequest(otp, "123456");
    request.setSignature("invalid-signature");

    assertFalse(facade.validate(request, OtpFlowType.AUTHENTICATION, "user@example.com"));

    verify(otpService).incrementAttempt(otp);
    verify(otpService, never()).delete(otp);
  }

  @Test
  void validateDoesNotMutateOtpWhenDataDoesNotMatch() throws Exception {
    OtpService otpService = mock(OtpService.class);
    EmailOtpFacade facade = facade(otpService);
    Otp otp = otp(OtpFlowType.AUTHENTICATION, "user@example.com");
    when(otpService.getById(otp.getId())).thenReturn(otp);

    assertFalse(facade.validate(otpRequest(otp, "123456"), OtpFlowType.AUTHENTICATION, "other@example.com"));

    verify(otpService, never()).incrementAttempt(otp);
    verify(otpService, never()).delete(otp);
  }

  private static EmailOtpFacade facade(OtpService otpService) {
    EmailOtpProperties otpProperties = new EmailOtpProperties();
    otpProperties.setAttemptCount(3);
    otpProperties.setLength(6);
    otpProperties.setTtl(Duration.ofMinutes(3));
    EmailOtpFacade facade = new EmailOtpFacade(
        otpService,
        mock(EmailSender.class),
        mock(RateLimiter.class),
        otpProperties,
        new RateLimitProperties());
    ReflectionTestUtils.setField(facade, "encryptionKey", ENCRYPTION_KEY);
    return facade;
  }

  private static Otp otp(OtpFlowType flow, String data) {
    Otp otp = new Otp();
    otp.setId(UUID.randomUUID());
    otp.setFlow(flow);
    otp.setData(data);
    otp.setExpiresAt(Instant.now().plusSeconds(300));
    return otp;
  }

  private static OtpRequest otpRequest(Otp otp, String value) throws Exception {
    OtpRequest request = new OtpRequest();
    request.setId(otp.getId());
    request.setValue(value);
    request.setSignature(HMacUtil.hmac(
        ENCRYPTION_KEY,
        String.format("%s:%s:%s:%s", otp.getId(), otp.getFlow().name(), otp.getData(), value)));
    return request;
  }
}
