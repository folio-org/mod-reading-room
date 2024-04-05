package org.folio.readingroom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = {"readingRoom"})
@Table(name = "reading_room_service_point")
public class ReadingRoomServicePointEntity {

  @Id
  @Column(name = "service_point_id", nullable = false)
  private UUID id;

  @Column(name = "service_point_name")
  private String name;

  @ManyToOne
  @JoinColumn(name = "reading_room_id", nullable = false)
  private ReadingRoomEntity readingRoom;

}
