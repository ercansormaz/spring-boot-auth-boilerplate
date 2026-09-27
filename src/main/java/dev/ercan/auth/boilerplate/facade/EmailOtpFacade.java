package dev.ercan.auth.boilerplate.facade;

import dev.ercan.auth.boilerplate.config.property.EmailOtpProperties;
import dev.ercan.auth.boilerplate.dto.request.OtpRequest;
import dev.ercan.auth.boilerplate.dto.response.OtpResponse;
import dev.ercan.auth.boilerplate.exception.RateLimitExceedException;
import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
import dev.ercan.auth.boilerplate.model.pojo.RateLimitPolicy;
import dev.ercan.auth.boilerplate.model.pojo.RateLimitResult;
import dev.ercan.auth.boilerplate.service.OtpService;
import dev.ercan.auth.boilerplate.service.port.EmailSender;
import dev.ercan.auth.boilerplate.service.port.RateLimiter;
import dev.ercan.auth.boilerplate.util.EmailNormalizer;
import dev.ercan.auth.boilerplate.util.HMacUtil;
import dev.ercan.auth.boilerplate.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOtpFacade {

  private static final String RETRY_LIMIT_KEY = "otp:email:retry:%s:%s";
  private static final String RATE_LIMIT_KEY = "otp:email:%s:%s";
  private static final String HMAC_DATA = "%s:%s:%s:%s";

  @Value("${otp.encryption.key}")
  private String encryptionKey;

  private final OtpService otpService;
  private final EmailSender emailSender;
  private final RateLimiter rateLimiter;

  private final EmailOtpProperties emailOtpProperties;

  public OtpResponse createAndSendOtp(OtpFlowType flow, String rawEmail) {
    final String email = EmailNormalizer.normalize(rawEmail);

    RateLimitResult rateLimitResult = consumeRateLimits(flow, email);

    String otpValue = RandomUtil.number(emailOtpProperties.getLength());
    Otp otp = otpService.replaceExistingOtp(flow, email, emailOtpProperties.getTtl());

    // @formatter:off
    OtpDetail otpDetail = createSignedOtpDetail(
        otp,
        otpValue,
        emailOtpProperties.getTtl(),
        rateLimitResult.remainingTokens(),
        emailOtpProperties.getRetryDelay()
    );
    // @formatter:on

    emailSender.sendOtp(otpDetail);

    return new OtpResponse(otpDetail);

  }

  @Transactional
  public boolean validate(OtpRequest otpRequest, OtpFlowType flow, String dataToVerify) {
    Otp otp = otpService.getById(otpRequest.getId());

    if (Objects.isNull(otp) || Instant.now().isAfter(otp.getExpiresAt())
        || otp.getAttemptCount() >= emailOtpProperties.getAttemptCount()) {
      return false;
    }

    if (!flow.equals(otp.getFlow()) || !dataToVerify.equals(otp.getData())) {
      return false;
    }

    String hmac = sign(otp.getId(), flow, dataToVerify, otpRequest.getValue());
    if (MessageDigest.isEqual(otpRequest.getSignature().getBytes(UTF_8), hmac.getBytes(UTF_8))) {
      otpService.delete(otp);
      return true;
    }

    otpService.incrementAttempt(otp);

    return false;
  }

  private RateLimitResult consumeRateLimits(OtpFlowType flow, String email) {
    String retryLimitKey = String.format(RETRY_LIMIT_KEY, flow.name(), email);
    Duration retryDelay = emailOtpProperties.getRetryDelay();

    if (!rateLimiter.tryConsume(retryLimitKey, 1, retryDelay)) {
      throw new RateLimitExceedException(RateLimitType.EMAIL_OTP_REQUESTED.getErrorType());
    }

    RateLimitPolicy policy = emailOtpProperties.getPerEmailPolicy();
    String rateLimitKey = String.format(RATE_LIMIT_KEY, flow.name(), email);

    RateLimitResult result = rateLimiter.tryConsumeAndGet(rateLimitKey, policy.limit(), policy.window());
    if (!result.consumed()) {
      rateLimiter.rollback(retryLimitKey, 1, retryDelay);
      throw new RateLimitExceedException(RateLimitType.EMAIL_OTP_REQUESTED.getErrorType());
    }

    return result;
  }

  private OtpDetail createSignedOtpDetail(Otp otp, String value, Duration duration, long remainingRetryCount,
      Duration retryDuration) {
    String signature = sign(otp.getId(), otp.getFlow(), otp.getData(), value);
    return new OtpDetail(otp.getId(), signature, otp.getData(), value, otp.getFlow(), value.length(),
        duration.toSeconds(), remainingRetryCount, retryDuration.toSeconds());
  }

  private String sign(UUID otpId, OtpFlowType flow, String dataToVerify, String value) {
    try {
      return HMacUtil.hmac(encryptionKey, String.format(HMAC_DATA, otpId, flow.name(), dataToVerify, value));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("[EMAIL_OTP_FACADE] [HMAC_GENERATION_FAILED]", e);
      throw new IllegalStateException("Failed to generate OTP signature", e);
    }
  }
}
