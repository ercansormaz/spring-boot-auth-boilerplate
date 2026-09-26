package dev.ercan.auth.boilerplate.job;

import dev.ercan.auth.boilerplate.facade.AuthenticationTokenFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccessTokenCleanerJob {

  private final AuthenticationTokenFacade authenticationTokenFacade;

  @Value("${auth.token.access.cleaner-job.fetch-count}")
  private int fetchCount;

  @Value("${auth.token.access.cleaner-job.max-iteration}")
  private int maxIteration;

  @Scheduled(cron = "${auth.token.access.cleaner-job.cron}")
  public void execute() {
    log.info("[ACCESS_TOKEN_CLEANER_JOB] [STARTED]");
    int totalDeleted = 0;
    int iteration = 0;

    while (iteration < maxIteration) {
      try {
        int deletedCount = authenticationTokenFacade.deleteExpiredAccessTokens(fetchCount);
        if (deletedCount == 0) {
          break;
        }
        totalDeleted += deletedCount;
      } catch (Exception e) {
        log.error("[ACCESS_TOKEN_CLEANER_JOB] [FAILED] [ITERATION_COUNT={}]", iteration, e);
      }
      iteration++;
    }

    log.info("[ACCESS_TOKEN_CLEANER_JOB] [FINISHED] [DELETED_TOKEN_COUNT={}]", totalDeleted);
  }

}
