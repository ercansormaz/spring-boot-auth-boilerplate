package dev.ercan.auth.boilerplate.model.entity;

import dev.ercan.auth.boilerplate.model.enums.DevicePlatform;
import dev.ercan.auth.boilerplate.model.enums.DeviceType;
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
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Audited
@Table(name = "device", indexes = {
    @Index(name = "idx_device_account_identifier", columnList = "account_id, identifier", unique = true)
})
public class Device {

  @Id
  private UUID id;

  @NotAudited
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "fk_device_account"))
  private Account account;

  @NotAudited
  private String identifier;

  @NotAudited
  private String model;

  @NotAudited
  @Enumerated(EnumType.STRING)
  private DevicePlatform platform;

  @NotAudited
  @Enumerated(EnumType.STRING)
  private DeviceType type;

  @Column(length = 2)
  private String language;

  private String appVersion;

  private String osVersion;

  @NotAudited
  private boolean active;

  @NotAudited
  @CreationTimestamp
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;

  @PrePersist
  private void prePersist() {
    if (id == null) {
      id = UUIDv7.randomUUID();
    }
  }


}
