package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.QrCode;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QrCodeRepository extends JpaRepository<QrCode, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
  Optional<QrCode> findById(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({
      @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2") // "-2" for "SKIP LOCKED"
  })
  @Query("SELECT qr FROM QrCode qr WHERE qr.expiresAt < ?1")
  List<QrCode> findByExpiresAtBefore(Instant expiresAt, Pageable pageable);

}
