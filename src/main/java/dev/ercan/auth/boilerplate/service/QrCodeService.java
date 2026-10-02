package dev.ercan.auth.boilerplate.service;

import dev.ercan.auth.boilerplate.exception.BadRequestException;
import dev.ercan.auth.boilerplate.model.entity.Account;
import dev.ercan.auth.boilerplate.model.entity.QrCode;
import dev.ercan.auth.boilerplate.model.entity.QrCode.QrCodeStatus;
import dev.ercan.auth.boilerplate.model.enums.ErrorType;
import dev.ercan.auth.boilerplate.repository.QrCodeRepository;
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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QrCodeService {

  private final QrCodeRepository qrCodeRepository;

  public QrCode create(Duration duration) {
    QrCode qrCode = new QrCode();
    qrCode.setNonce(UUID.randomUUID());
    qrCode.setStatus(QrCodeStatus.PENDING);
    qrCode.setExpiresAt(Instant.now().plus(duration));
    return qrCodeRepository.save(qrCode);
  }

  @Transactional
  public Optional<Account> consumeIfApproved(UUID qrId) {
    QrCode qrCode = getById(qrId);
    if (Objects.isNull(qrCode)) {
      throw new BadRequestException(ErrorType.INVALID_QR_CODE);
    }

    if (Instant.now().isAfter(qrCode.getExpiresAt())) {
      delete(qrCode);
      throw new BadRequestException(ErrorType.EXPIRED_QR_CODE);
    }

    if (QrCodeStatus.APPROVED.equals(qrCode.getStatus())) {
      Account account = qrCode.getAccount();
      delete(qrCode);
      return Optional.of(account);
    }

    return Optional.empty();
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public QrCode save(QrCode qrCode) {
    return qrCodeRepository.save(qrCode);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public QrCode getById(UUID id) {
    return qrCodeRepository.findById(id).orElse(null);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void delete(QrCode qrCode) {
    qrCodeRepository.delete(qrCode);
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public int deleteExpiredQrCodes(int fetchCount) {
    List<QrCode> qrCodesToExpire = qrCodeRepository.findByExpiresAtBefore(Instant.now(), PageRequest.of(0, fetchCount));

    if (CollectionUtils.isEmpty(qrCodesToExpire)) {
      return 0;
    }

    qrCodeRepository.deleteAll(qrCodesToExpire);

    return qrCodesToExpire.size();
  }

}