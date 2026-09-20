package dev.ercan.auth.boilerplate.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "access_token", indexes = {
    @Index(name = "idx_access_token_device", columnList = "device_id", unique = true),
    @Index(name = "idx_access_token_expire", columnList = "expires_at")
})
public class AccessToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "device_id", foreignKey = @ForeignKey(name = "fk_access_token_device"))
  private Device device;

  private String salt;

  @CreationTimestamp
  private Instant createdAt;

  private Instant expiresAt;


}
