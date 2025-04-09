package org.folio.readingroom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.folio.readingroom.domain.base.AuditableEntity;
import org.folio.readingroom.domain.dto.AccessLog.ActionEnum;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "access_log")
public class AccessLogEntity extends AuditableEntity {

  @Id
  @Column(nullable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "patron_id", nullable = false)
  private UUID patronId;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "action", columnDefinition = "accessLogActionEnum", nullable = false)
  private ActionEnum action;

  @Column(name = "reading_room_id", nullable = false)
  private UUID readingRoomId;

  @Column(name = "reading_room_name", nullable = false)
  private String readingRoomName;

  @Column(name = "service_point_id", nullable = false)
  private UUID servicePointId;

}
