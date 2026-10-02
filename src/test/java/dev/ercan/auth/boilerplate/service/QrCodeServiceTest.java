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

import dev.ercan.auth.boilerplate.exception.BadRequestException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.QrCode;
import dev.ercan.auth.boilerplate.model.entity.QrCode.QrCodeStatus;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.repository.QrCodeRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

class QrCodeServiceTest {

  @Test
  void createsPendingQrCodeWithNonceAndExpiration() {
    QrCodeRepository repository = mock(QrCodeRepository.class);
    QrCodeService service = new QrCodeService(repository);
    when(repository.save(any(QrCode.class))).thenAnswer(invocation -> invocation.getArgument(0));
    Instant before = Instant.now();

    QrCode qrCode = service.create(Duration.ofMinutes(2));

    assertEquals(QrCodeStatus.PENDING, qrCode.getStatus());
    assertNotNull(qrCode.getNonce());
    assertTrue(qrCode.getExpiresAt().isAfter(before.plusSeconds(115)));
    assertTrue(qrCode.getExpiresAt().isBefore(before.plusSeconds(125)));
    verify(repository).save(qrCode);
  }

  @Test
  void returnsEmptyWithoutDeletingPendingQrCode() {
    QrCodeRepository repository = mock(QrCodeRepository.class);
    QrCodeService service = new QrCodeService(repository);
    QrCode qrCode = qrCode(QrCodeStatus.PENDING, Instant.now().plusSeconds(60));
    when(repository.findById(qrCode.getId())).thenReturn(Optional.of(qrCode));

    assertTrue(service.consumeIfApproved(qrCode.getId()).isEmpty());

    verify(repository, never()).delete(qrCode);
  }

  @Test
  void consumesAndDeletesApprovedQrCode() {
    QrCodeRepository repository = mock(QrCodeRepository.class);
    QrCodeService service = new QrCodeService(repository);
    Account account = new Account();
    QrCode qrCode = qrCode(QrCodeStatus.APPROVED, Instant.now().plusSeconds(60));
    qrCode.setAccount(account);
    when(repository.findById(qrCode.getId())).thenReturn(Optional.of(qrCode));

    assertSame(account, service.consumeIfApproved(qrCode.getId()).orElseThrow());

    verify(repository).delete(qrCode);
  }

  @Test
  void deletesExpiredQrCodeAndReportsExpiration() {
    QrCodeRepository repository = mock(QrCodeRepository.class);
    QrCodeService service = new QrCodeService(repository);
    QrCode qrCode = qrCode(QrCodeStatus.PENDING, Instant.now().minusSeconds(1));
    when(repository.findById(qrCode.getId())).thenReturn(Optional.of(qrCode));

    BadRequestException exception = assertThrows(
        BadRequestException.class, () -> service.consumeIfApproved(qrCode.getId()));

    assertEquals(ErrorType.EXPIRED_QR_CODE, exception.getErrorType());
    verify(repository).delete(qrCode);
  }

  @Test
  void rejectsUnknownQrCode() {
    QrCodeRepository repository = mock(QrCodeRepository.class);
    QrCodeService service = new QrCodeService(repository);
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    BadRequestException exception = assertThrows(
        BadRequestException.class, () -> service.consumeIfApproved(id));

    assertEquals(ErrorType.INVALID_QR_CODE, exception.getErrorType());
  }

  @Test
  void deletesExpiredBatchAndReturnsItsSize() {
    QrCodeRepository repository = mock(QrCodeRepository.class);
    QrCodeService service = new QrCodeService(repository);
    List<QrCode> expired = List.of(new QrCode(), new QrCode());
    when(repository.findByExpiresAtBefore(any(Instant.class), any(Pageable.class))).thenReturn(expired);

    assertEquals(2, service.deleteExpiredQrCodes(10));

    verify(repository).deleteAll(expired);
  }

  private static QrCode qrCode(QrCodeStatus status, Instant expiresAt) {
    QrCode qrCode = new QrCode();
    qrCode.setId(UUID.randomUUID());
    qrCode.setNonce(UUID.randomUUID());
    qrCode.setStatus(status);
    qrCode.setExpiresAt(expiresAt);
    return qrCode;
  }
}
