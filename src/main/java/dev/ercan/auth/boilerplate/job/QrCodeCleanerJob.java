package dev.ercan.auth.boilerplate.job;

import dev.ercan.auth.boilerplate.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "auth.qr.enabled", havingValue = "true")
public class QrCodeCleanerJob {

  private final QrCodeService qrCodeService;

  @Value("${auth.qr.cleaner-job.fetch-count}")
  private int fetchCount;

  @Value("${auth.qr.cleaner-job.max-iteration}")
  private int maxIteration;

  @Scheduled(cron = "${auth.qr.cleaner-job.cron}")
  public void execute() {
    log.info("[QR_CODE_CLEANER_JOB] [STARTED]");
    int totalDeleted = 0;
    int iteration = 0;

    while (iteration < maxIteration) {
      try {
        int deletedCount = qrCodeService.deleteExpiredQrCodes(fetchCount);
        if (deletedCount == 0) {
          break;
        }
        totalDeleted += deletedCount;
      } catch (Exception e) {
        log.error("[QR_CODE_CLEANER_JOB] [FAILED] [ITERATION_COUNT={}]", iteration, e);
      }
      iteration++;
    }

    log.info("[QR_CODE_CLEANER_JOB] [FINISHED] [DELETED_TOKEN_COUNT={}]", totalDeleted);
  }

}
