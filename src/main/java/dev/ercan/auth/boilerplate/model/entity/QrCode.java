package dev.ercan.auth.boilerplate.model.entity;

import dev.ercan.auth.boilerplate.util.UUIDv7;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "qr_code", indexes = {
    @Index(name = "idx_qr_code_expire", columnList = "expires_at")
})
public class QrCode implements Serializable {

  @Id
  private UUID id;

  private UUID nonce;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(255)")
  private QrCodeStatus status = QrCodeStatus.PENDING;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "fk_qr_code_account"))
  private Account account;

  @CreationTimestamp
  private Instant createdAt;

  private Instant expiresAt;

  @PrePersist
  private void prePersist() {
    if (id == null) {
      id = UUIDv7.randomUUID();
    }
  }

  public enum QrCodeStatus {
    PENDING,
    APPROVED
  }

}
