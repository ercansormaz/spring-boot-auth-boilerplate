package dev.ercan.auth.boilerplate.job;

import dev.ercan.auth.boilerplate.facade.AuthenticationTokenFacade;
import dev.ercan.auth.boilerplate.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CleanerJobsTest {

  @Test
  void accessTokenCleanerStopsWhenNoExpiredTokensRemain() {
    AuthenticationTokenFacade facade = mock(AuthenticationTokenFacade.class);
    when(facade.deleteExpiredAccessTokens(25)).thenReturn(0);
    AccessTokenCleanerJob job = accessTokenCleaner(facade, 25, 5);

    job.execute();

    verify(facade).deleteExpiredAccessTokens(25);
  }

  @Test
  void accessTokenCleanerStopsAtMaximumIterationCount() {
    AuthenticationTokenFacade facade = mock(AuthenticationTokenFacade.class);
    when(facade.deleteExpiredAccessTokens(25)).thenReturn(25);
    AccessTokenCleanerJob job = accessTokenCleaner(facade, 25, 3);

    job.execute();

    verify(facade, times(3)).deleteExpiredAccessTokens(25);
  }

  @Test
  void accessTokenCleanerContinuesAfterIterationFailure() {
    AuthenticationTokenFacade facade = mock(AuthenticationTokenFacade.class);
    when(facade.deleteExpiredAccessTokens(25))
        .thenThrow(new IllegalStateException("temporary failure"))
        .thenReturn(1)
        .thenReturn(0);
    AccessTokenCleanerJob job = accessTokenCleaner(facade, 25, 5);

    job.execute();

    verify(facade, times(3)).deleteExpiredAccessTokens(25);
  }

  @Test
  void refreshTokenCleanerStopsWhenNoExpiredTokensRemain() {
    AuthenticationTokenFacade facade = mock(AuthenticationTokenFacade.class);
    when(facade.deleteExpiredRefreshTokens(30)).thenReturn(0);
    RefreshTokenCleanerJob job = refreshTokenCleaner(facade, 30, 5);

    job.execute();

    verify(facade).deleteExpiredRefreshTokens(30);
  }

  @Test
  void refreshTokenCleanerStopsAtMaximumIterationCount() {
    AuthenticationTokenFacade facade = mock(AuthenticationTokenFacade.class);
    when(facade.deleteExpiredRefreshTokens(30)).thenReturn(30);
    RefreshTokenCleanerJob job = refreshTokenCleaner(facade, 30, 2);

    job.execute();

    verify(facade, times(2)).deleteExpiredRefreshTokens(30);
  }

  @Test
  void refreshTokenCleanerContinuesAfterIterationFailure() {
    AuthenticationTokenFacade facade = mock(AuthenticationTokenFacade.class);
    when(facade.deleteExpiredRefreshTokens(30))
        .thenThrow(new IllegalStateException("temporary failure"))
        .thenReturn(1)
        .thenReturn(0);
    RefreshTokenCleanerJob job = refreshTokenCleaner(facade, 30, 5);

    job.execute();

    verify(facade, times(3)).deleteExpiredRefreshTokens(30);
  }

  @Test
  void otpCleanerStopsWhenNoExpiredOtpsRemain() {
    OtpService otpService = mock(OtpService.class);
    when(otpService.deleteExpiredOtps(40)).thenReturn(0);
    OtpCleanerJob job = otpCleaner(otpService, 40, 5);

    job.execute();

    verify(otpService).deleteExpiredOtps(40);
  }

  @Test
  void otpCleanerStopsAtMaximumIterationCount() {
    OtpService otpService = mock(OtpService.class);
    when(otpService.deleteExpiredOtps(40)).thenReturn(40);
    OtpCleanerJob job = otpCleaner(otpService, 40, 4);

    job.execute();

    verify(otpService, times(4)).deleteExpiredOtps(40);
  }

  @Test
  void otpCleanerContinuesAfterIterationFailure() {
    OtpService otpService = mock(OtpService.class);
    when(otpService.deleteExpiredOtps(40))
        .thenThrow(new IllegalStateException("temporary failure"))
        .thenReturn(1)
        .thenReturn(0);
    OtpCleanerJob job = otpCleaner(otpService, 40, 5);

    job.execute();

    verify(otpService, times(3)).deleteExpiredOtps(40);
  }

  private static AccessTokenCleanerJob accessTokenCleaner(
      AuthenticationTokenFacade facade, int fetchCount, int maxIteration) {
    AccessTokenCleanerJob job = new AccessTokenCleanerJob(facade);
    ReflectionTestUtils.setField(job, "fetchCount", fetchCount);
    ReflectionTestUtils.setField(job, "maxIteration", maxIteration);
    return job;
  }

  private static RefreshTokenCleanerJob refreshTokenCleaner(
      AuthenticationTokenFacade facade, int fetchCount, int maxIteration) {
    RefreshTokenCleanerJob job = new RefreshTokenCleanerJob(facade);
    ReflectionTestUtils.setField(job, "fetchCount", fetchCount);
    ReflectionTestUtils.setField(job, "maxIteration", maxIteration);
    return job;
  }

  private static OtpCleanerJob otpCleaner(OtpService service, int fetchCount, int maxIteration) {
    OtpCleanerJob job = new OtpCleanerJob(service);
    ReflectionTestUtils.setField(job, "fetchCount", fetchCount);
    ReflectionTestUtils.setField(job, "maxIteration", maxIteration);
    return job;
  }
}
