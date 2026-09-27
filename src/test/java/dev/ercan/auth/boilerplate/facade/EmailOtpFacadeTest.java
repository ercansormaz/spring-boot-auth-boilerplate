package dev.ercan.auth.boilerplate.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.config.property.EmailOtpProperties;
import dev.ercan.auth.boilerplate.dto.request.OtpRequest;
import dev.ercan.auth.boilerplate.dto.response.OtpResponse;
import dev.ercan.auth.boilerplate.exception.RateLimitExceedException;
import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
import dev.ercan.auth.boilerplate.model.pojo.RateLimitPolicy;
import dev.ercan.auth.boilerplate.model.pojo.RateLimitResult;
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

  @Test
  void createAndSendOtpConsumesBothLimitsAndReturnsRetryDetails() {
    OtpService otpService = mock(OtpService.class);
    EmailSender emailSender = mock(EmailSender.class);
    RateLimiter rateLimiter = mock(RateLimiter.class);
    EmailOtpProperties properties = otpProperties();
    EmailOtpFacade facade = facade(otpService, emailSender, rateLimiter, properties);
    Otp otp = otp(OtpFlowType.AUTHENTICATION, "user@example.com");
    Duration retryDelay = properties.getRetryDelay();
    Duration policyWindow = properties.getPerEmailPolicy().window();
    when(rateLimiter.tryConsume("otp:email:retry:AUTHENTICATION:user@example.com", 1, retryDelay))
        .thenReturn(true);
    when(rateLimiter.tryConsumeAndGet(
        "otp:email:AUTHENTICATION:user@example.com", 3, policyWindow))
        .thenReturn(new RateLimitResult(true, 2, Duration.ZERO));
    when(otpService.replaceExistingOtp(OtpFlowType.AUTHENTICATION, "user@example.com", properties.getTtl()))
        .thenReturn(otp);

    OtpResponse response = facade.createAndSendOtp(OtpFlowType.AUTHENTICATION, " USER@EXAMPLE.COM ");

    assertTrue(response.isRetryable());
    assertEquals(Long.valueOf(retryDelay.toSeconds()), response.getRetryIn());
    assertEquals(properties.getTtl().toSeconds(), response.getExpiresIn());
    verify(otpService).replaceExistingOtp(OtpFlowType.AUTHENTICATION, "user@example.com", properties.getTtl());
    verify(emailSender).sendOtp(any(OtpDetail.class));
  }

  @Test
  void createAndSendOtpDoesNotContinueWhenRetryCooldownIsActive() {
    OtpService otpService = mock(OtpService.class);
    RateLimiter rateLimiter = mock(RateLimiter.class);
    EmailOtpProperties properties = otpProperties();
    EmailOtpFacade facade = facade(otpService, mock(EmailSender.class), rateLimiter, properties);
    when(rateLimiter.tryConsume(
        "otp:email:retry:AUTHENTICATION:user@example.com", 1, properties.getRetryDelay()))
        .thenReturn(false);

    assertThrows(RateLimitExceedException.class,
        () -> facade.createAndSendOtp(OtpFlowType.AUTHENTICATION, "user@example.com"));

    verify(rateLimiter, never()).tryConsumeAndGet(any(), org.mockito.ArgumentMatchers.anyInt(), any());
    verify(otpService, never()).replaceExistingOtp(any(), any(), any());
  }

  @Test
  void createAndSendOtpRollsBackRetryTokenWhenPerEmailLimitIsReached() {
    OtpService otpService = mock(OtpService.class);
    RateLimiter rateLimiter = mock(RateLimiter.class);
    EmailOtpProperties properties = otpProperties();
    EmailOtpFacade facade = facade(otpService, mock(EmailSender.class), rateLimiter, properties);
    String retryKey = "otp:email:retry:AUTHENTICATION:user@example.com";
    String rateLimitKey = "otp:email:AUTHENTICATION:user@example.com";
    Duration retryDelay = properties.getRetryDelay();
    Duration policyWindow = properties.getPerEmailPolicy().window();
    when(rateLimiter.tryConsume(retryKey, 1, retryDelay)).thenReturn(true);
    when(rateLimiter.tryConsumeAndGet(rateLimitKey, 3, policyWindow))
        .thenReturn(new RateLimitResult(false, 0, policyWindow));

    assertThrows(RateLimitExceedException.class,
        () -> facade.createAndSendOtp(OtpFlowType.AUTHENTICATION, "user@example.com"));

    verify(rateLimiter).rollback(retryKey, 1, retryDelay);
    verify(otpService, never()).replaceExistingOtp(any(), any(), any());
  }

  @Test
  void createAndSendOtpOmitsRetryDetailsWhenPerEmailQuotaIsExhausted() {
    OtpService otpService = mock(OtpService.class);
    EmailSender emailSender = mock(EmailSender.class);
    RateLimiter rateLimiter = mock(RateLimiter.class);
    EmailOtpProperties properties = otpProperties();
    EmailOtpFacade facade = facade(otpService, emailSender, rateLimiter, properties);
    Otp otp = otp(OtpFlowType.AUTHENTICATION, "user@example.com");
    when(rateLimiter.tryConsume(any(), org.mockito.ArgumentMatchers.anyInt(), any())).thenReturn(true);
    when(rateLimiter.tryConsumeAndGet(any(), org.mockito.ArgumentMatchers.anyInt(), any()))
        .thenReturn(new RateLimitResult(true, 0, Duration.ZERO));
    when(otpService.replaceExistingOtp(any(), any(), any())).thenReturn(otp);

    OtpResponse response = facade.createAndSendOtp(OtpFlowType.AUTHENTICATION, "user@example.com");

    assertFalse(response.isRetryable());
    assertNull(response.getRetryIn());
  }

  private static EmailOtpFacade facade(OtpService otpService) {
    return facade(otpService, mock(EmailSender.class), mock(RateLimiter.class), otpProperties());
  }

  private static EmailOtpFacade facade(OtpService otpService, EmailSender emailSender,
      RateLimiter rateLimiter, EmailOtpProperties otpProperties) {
    EmailOtpFacade facade = new EmailOtpFacade(
        otpService,
        emailSender,
        rateLimiter,
        otpProperties);
    ReflectionTestUtils.setField(facade, "encryptionKey", ENCRYPTION_KEY);
    return facade;
  }

  private static EmailOtpProperties otpProperties() {
    EmailOtpProperties otpProperties = new EmailOtpProperties();
    otpProperties.setAttemptCount(3);
    otpProperties.setLength(6);
    otpProperties.setTtl(Duration.ofMinutes(3));
    otpProperties.setRetryDelay(Duration.ofSeconds(30));
    otpProperties.setPerEmailPolicy(new RateLimitPolicy(3, Duration.ofHours(24)));
    return otpProperties;
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
