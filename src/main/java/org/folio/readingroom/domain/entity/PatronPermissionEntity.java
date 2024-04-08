package org.folio.readingroom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.folio.readingroom.domain.base.AuditableEntity;
import org.folio.readingroom.domain.dto.PatronPermission;

@Entity
@Getter
@Setter
@Table(name = "patron_permission")
public class PatronPermissionEntity extends AuditableEntity {
  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reading_room_id", nullable = false)
  private ReadingRoomEntity readingRoom;

  @Column(name = "notes", columnDefinition = "text")
  private String notes;

  @Enumerated(EnumType.STRING)
  @Column(name = "access", nullable = false)
  private PatronPermission.AccessEnum access;
}
