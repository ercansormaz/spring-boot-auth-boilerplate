package dev.ercan.auth.boilerplate.model.entity;

import dev.ercan.auth.boilerplate.model.enums.AuthProviderType;
import jakarta.persistence.Column;
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
@Table(name = "account_identity", indexes = {
    @Index(name = "idx_account_identity", columnList = "provider, subject", unique = true)
})
public class AccountIdentity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "account_id", foreignKey = @ForeignKey(name = "fk_account_identity_account"))
  private Account account;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(255)")
  private AuthProviderType provider;

  private String subject;

  @CreationTimestamp
  private Instant createdAt;

}
