package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.Otp;
import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
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

public interface OtpRepository extends JpaRepository<Otp, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
  Optional<Otp> findById(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
  List<Otp> findByFlowAndData(OtpFlowType flow, String data);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({
      @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2") // "-2" for "SKIP LOCKED"
  })
  @Query("SELECT o FROM Otp o WHERE o.expiresAt < ?1")
  List<Otp> findByExpiresAtBefore(Instant expiresAt, Pageable pageable);

}
