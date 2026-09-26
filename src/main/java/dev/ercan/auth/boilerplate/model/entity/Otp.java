package dev.ercan.auth.boilerplate.model.entity;

import dev.ercan.auth.boilerplate.model.enums.OtpFlowType;
import dev.ercan.auth.boilerplate.util.UUIDv7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "otp", indexes = {
    @Index(name = "idx_otp_expire", columnList = "expires_at"),
    @Index(name = "idx_otp_flow_data", columnList = "flow, data")
})
public class Otp {

  @Id
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(255)")
  private OtpFlowType flow;

  private String data;

  private int attemptCount = 0;

  @CreationTimestamp
  private Instant createdAt;

  private Instant expiresAt;

  public void incrementAttempt() {
    this.attemptCount++;
  }

  @PrePersist
  private void prePersist() {
    if (id == null) {
      id = UUIDv7.randomUUID();
    }
  }

}
