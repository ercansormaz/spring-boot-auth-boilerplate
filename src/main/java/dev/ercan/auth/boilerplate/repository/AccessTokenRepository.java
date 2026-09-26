package dev.ercan.auth.boilerplate.repository;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Device;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import java.time.Instant;
import java.util.List;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

  AccessToken findByDevice(Device device);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @QueryHints({
      @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2") // "-2" for "SKIP LOCKED"
  })
  @Query("SELECT a FROM AccessToken a JOIN FETCH a.device WHERE a.expiresAt < ?1")
  List<AccessToken> findByExpiresAtBefore(Instant expiresAt, Pageable pageable);
  
}
