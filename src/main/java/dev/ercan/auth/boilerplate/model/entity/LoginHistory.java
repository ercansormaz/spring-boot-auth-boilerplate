package dev.ercan.auth.boilerplate.model.entity;

import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "login_history", indexes = {
    @Index(name = "idx_login_history_device", columnList = "device_id")
})
public class LoginHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "device_id", foreignKey = @ForeignKey(name = "fk_login_history_device"))
  private Device device;

  @CreationTimestamp
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  private AuthProviderType type;

}
