package dev.ercan.auth.boilerplate.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import java.io.Serializable;

@Getter
@Setter
@Entity
@RevisionEntity
public class AuditRevision implements Serializable {

  @Id
  @RevisionNumber
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @RevisionTimestamp
  private Long timestamp;

}