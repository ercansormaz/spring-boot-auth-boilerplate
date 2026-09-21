package dev.ercan.auth.boilerplate.facade;

import dev.ercan.auth.boilerplate.config.EmailOtpProperties;
import dev.ercan.auth.boilerplate.config.RateLimitProperties;
import dev.ercan.auth.boilerplate.config.RateLimitProperties.Policy;
import dev.ercan.auth.boilerplate.dto.request.OtpRequest;
import dev.ercan.auth.boilerplate.dto.response.OtpResponse;
import dev.ercan.auth.boilerplate.exception.RateLimitExceedException;
import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.model.enums.RateLimitScope;
import dev.ercan.auth.boilerplate.model.enums.RateLimitType;
import dev.ercan.auth.boilerplate.model.pojo.OtpDetail;
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
import java.util.Objects;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailOtpFacade {

  private static final String RATE_LIMIT_KEY = "otp:email:%s";
  private static final String HMAC_DATA = "%s:%s:%s:%s";

  @Value("${otp.encryption.key}")
  private String encryptionKey;

  private final OtpService otpService;
  private final EmailSender emailSender;
  private final RateLimiter rateLimiter;

  private final EmailOtpProperties emailOtpProperties;
  private final RateLimitProperties rateLimitProperties;

  public OtpResponse createAndSendOtp(OtpFlowType flow, String email) {
    email = EmailNormalizer.normalize(email);

    Policy policy = rateLimitProperties.getPolicyByTypeAndScope(RateLimitType.EMAIL_OTP_REQUESTED, RateLimitScope.USER);

    String key = String.format(RATE_LIMIT_KEY, email);
    if (policy != null && policy.isEnabled() && !rateLimiter.tryConsume(key, policy.getLimit(), policy.getWindow())) {
      throw new RateLimitExceedException(RateLimitType.EMAIL_OTP_REQUESTED.getErrorType());
    }

    otpService.setSupersededPreviousOnes(flow, email);

    String otpValue = RandomUtil.number(emailOtpProperties.getLength());

    OtpDetail otpDetail = createAndGetOtpDetail(flow, email, otpValue, emailOtpProperties.getTtl());

    emailSender.sendOtp(otpDetail);

    return new OtpResponse(otpDetail);
  }

  @Transactional
  public boolean validate(OtpRequest otpRequest, OtpFlowType flow, String dataToVerify) {
    Otp otp = otpService.getById(otpRequest.getId());

    if (Objects.isNull(otp) || !otpService.isOtpValid(otp) || otp.getAttemptCount() >= emailOtpProperties.getAttemptCount()) {
      return false;
    }

    if (!flow.equals(otp.getFlow()) || !dataToVerify.equals(otp.getData())) {
      return false;
    }

    String hmac = sign(otp.getId(), flow, dataToVerify, otpRequest.getValue());
    if (MessageDigest.isEqual(otpRequest.getSignature().getBytes(UTF_8), hmac.getBytes(UTF_8))) {
      otpService.setUsed(otp);
      return true;
    }

    otpService.incrementAttempt(otp);

    return false;
  }

  private OtpDetail createAndGetOtpDetail(OtpFlowType flow, String dataToVerify, String value, Duration duration) {
    Otp otp = otpService.create(flow, dataToVerify, duration);

    String signature = sign(otp.getId(), flow, dataToVerify, value);

    return new OtpDetail(otp.getId(), signature, dataToVerify, value, flow, value.length(), duration.toSeconds());
  }

  private String sign(UUID otpId, OtpFlowType flow, String dataToVerify, String value) {
    try {
      return HMacUtil.hmac(encryptionKey, String.format(HMAC_DATA, otpId, flow.name(), dataToVerify, value));
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      log.error("[EMAIL_OTP_FACADE] HMAC generation failed", e);
      throw new IllegalStateException("Failed to generate OTP signature", e);
    }
  }
}
