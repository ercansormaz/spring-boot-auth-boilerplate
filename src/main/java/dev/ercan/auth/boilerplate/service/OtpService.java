package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.entity.Otp.Status;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OtpService {

  private final OtpRepository otpRepository;

  public Otp create(OtpFlowType flow, String dataToVerify, Duration duration) {
    Otp otp = new Otp();
    otp.setFlow(flow);
    otp.setData(dataToVerify);
    otp.setExpiresAt(Instant.now().plus(duration));
    otp.setStatus(Status.ACTIVE);
    return otpRepository.save(otp);
  }

  public Otp save(Otp otp) {
    return otpRepository.save(otp);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public Otp getById(UUID id) {
    return otpRepository.findById(id).orElse(null);
  }

  @Transactional
  public void setSupersededPreviousOnes(OtpFlowType flow, String data) {
    List<Otp> otpList = otpRepository.findByFlowAndDataAndStatus(flow, data, Status.ACTIVE);

    otpList.forEach(otp -> {
      otp.setStatus(Status.SUPERSEDED);
      otpRepository.save(otp);
    });
  }

  public Otp setUsed(Otp otp) {
    otp.setStatus(Status.USED);
    return otpRepository.save(otp);
  }

  public Otp incrementAttempt(Otp otp) {
    otp.incrementAttempt();
    return otpRepository.save(otp);
  }

  public boolean isOtpValid(Otp otp) {
    return Instant.now().isBefore(otp.getExpiresAt()) && Status.ACTIVE.equals(otp.getStatus());
  }
}
