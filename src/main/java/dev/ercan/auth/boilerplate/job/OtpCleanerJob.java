package dev.ercan.auth.boilerplate.job;

import dev.ercan.auth.boilerplate.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OtpCleanerJob {

  private final OtpService otpService;

  @Value("${otp.cleaner-job.fetch-count}")
  private int fetchCount;

  @Value("${otp.cleaner-job.max-iteration}")
  private int maxIteration;

  @Scheduled(cron = "${otp.cleaner-job.cron}")
  public void execute() {
    log.info("[OTP_CLEANER_JOB] [STARTED]");
    int totalDeleted = 0;
    int iteration = 0;

    while (iteration < maxIteration) {
      try {
        int deletedCount = otpService.deleteExpiredOtps(fetchCount);
        if (deletedCount == 0) {
          break;
        }
        totalDeleted += deletedCount;
      } catch (Exception e) {
        log.error("[OTP_CLEANER_JOB] [FAILED] [ITERATION_COUNT={}]", iteration, e);
      }
      iteration++;
    }

    log.info("[OTP_CLEANER_JOB] [FINISHED] [DELETED_TOKEN_COUNT={}]", totalDeleted);
  }

}
