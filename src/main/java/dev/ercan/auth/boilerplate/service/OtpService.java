package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

  private final OtpRepository otpRepository;

  @Transactional(propagation = Propagation.MANDATORY)
  public Otp getById(UUID id) {
    return otpRepository.findById(id).orElse(null);
  }

  @Transactional
  public Otp replaceExistingOtp(OtpFlowType flow, String data, Duration duration) {
    List<Otp> otpList = otpRepository.findByFlowAndData(flow, data);
    otpRepository.deleteAll(otpList);
    return create(flow, data, duration);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void delete(Otp otp) {
    otpRepository.delete(otp);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public int deleteExpiredOtps(int fetchCount) {
    List<Otp> otpsToExpire = otpRepository.findByExpiresAtBefore(Instant.now(), PageRequest.of(0, fetchCount));

    if (CollectionUtils.isEmpty(otpsToExpire)) {
      return 0;
    }

    otpRepository.deleteAll(otpsToExpire);

    return otpsToExpire.size();
  }

  public void incrementAttempt(Otp otp) {
    otp.incrementAttempt();
    otpRepository.save(otp);
  }

  private Otp create(OtpFlowType flow, String dataToVerify, Duration duration) {
    Otp otp = new Otp();
    otp.setFlow(flow);
    otp.setData(dataToVerify);
    otp.setNonce(UUID.randomUUID());
    otp.setExpiresAt(Instant.now().plus(duration));
    return otpRepository.save(otp);
  }

}
