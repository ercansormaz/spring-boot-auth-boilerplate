package dev.ercan.auth.boilerplate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.repository.OtpRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Pageable;

class OtpServiceTest {

  @Test
  void returnsOtpWhenIdExists() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    Otp otp = new Otp();
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.of(otp));

    assertSame(otp, service.getById(id));
  }

  @Test
  void replacesExistingOtpWithNewFlowDataAndExpiry() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    Otp oldOtp = new Otp();
    when(repository.findByFlowAndData(OtpFlowType.AUTHENTICATION, "user@example.com"))
        .thenReturn(List.of(oldOtp));
    when(repository.save(any(Otp.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Instant before = Instant.now();
    Otp replacement = service.replaceExistingOtp(
        OtpFlowType.AUTHENTICATION, "user@example.com", Duration.ofMinutes(3));

    verify(repository).deleteAll(List.of(oldOtp));
    ArgumentCaptor<Otp> savedOtp = ArgumentCaptor.forClass(Otp.class);
    verify(repository).save(savedOtp.capture());
    assertSame(replacement, savedOtp.getValue());
    assertEquals(OtpFlowType.AUTHENTICATION, replacement.getFlow());
    assertEquals("user@example.com", replacement.getData());
    assertNotNull(replacement.getNonce());
    assertTrue(replacement.getExpiresAt().isAfter(before.plusSeconds(170)));
    assertTrue(replacement.getExpiresAt().isBefore(before.plusSeconds(190)));
  }

  @Test
  void incrementsAttemptCountAndPersistsOtp() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    Otp otp = new Otp();

    service.incrementAttempt(otp);

    assertEquals(1, otp.getAttemptCount());
    verify(repository).save(otp);
  }

  @Test
  void replacementWithNoExistingOtpSkipsDeletionAndSavesNewOtp() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    when(repository.findByFlowAndData(OtpFlowType.AUTHENTICATION, "new@example.com")).thenReturn(List.of());
    when(repository.save(any(Otp.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Otp replacement = service.replaceExistingOtp(
        OtpFlowType.AUTHENTICATION, "new@example.com", Duration.ofMinutes(1));

    verify(repository).deleteAll(List.of());
    verify(repository).save(replacement);
    assertEquals("new@example.com", replacement.getData());
    assertNotNull(replacement.getNonce());
  }

  @Test
  void deleteExpiredOtpsReturnsZeroWithoutDeletingWhenRepositoryIsEmpty() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    when(repository.findByExpiresAtBefore(any(Instant.class), any(Pageable.class))).thenReturn(List.of());

    assertEquals(0, service.deleteExpiredOtps(10));

    verify(repository, never()).deleteAll(any());
  }

  @Test
  void deleteExpiredOtpsDeletesAndReturnsNumberOfExpiredRows() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    List<Otp> expired = List.of(new Otp(), new Otp());
    when(repository.findByExpiresAtBefore(any(Instant.class), any(Pageable.class))).thenReturn(expired);

    assertEquals(2, service.deleteExpiredOtps(10));

    verify(repository).deleteAll(expired);
  }

  @Test
  void deleteDelegatesToRepository() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    Otp otp = new Otp();

    service.delete(otp);

    verify(repository).delete(otp);
  }

  @Test
  void propagatesRepositoryFailureWhenLoadingExpiredOtps() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    DataAccessResourceFailureException failure = new DataAccessResourceFailureException("database unavailable");
    when(repository.findByExpiresAtBefore(any(Instant.class), any(Pageable.class))).thenThrow(failure);

    assertSame(failure, assertThrows(DataAccessResourceFailureException.class, () -> service.deleteExpiredOtps(5)));
  }

  @Test
  void doesNotCreateReplacementWhenExistingOtpLookupFails() {
    OtpRepository repository = mock(OtpRepository.class);
    OtpService service = new OtpService(repository);
    DataAccessResourceFailureException failure = new DataAccessResourceFailureException("database unavailable");
    when(repository.findByFlowAndData(OtpFlowType.AUTHENTICATION, "user@example.com")).thenThrow(failure);

    assertSame(failure, assertThrows(
        DataAccessResourceFailureException.class,
        () -> service.replaceExistingOtp(
            OtpFlowType.AUTHENTICATION, "user@example.com", Duration.ofMinutes(1))));

    verify(repository, never()).save(any(Otp.class));
  }
}
