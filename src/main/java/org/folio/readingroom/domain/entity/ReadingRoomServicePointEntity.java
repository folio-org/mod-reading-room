package org.folio.readingroom.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "reading_room_service_point")
public class ReadingRoomServicePointEntity {

  @Id
  @Column(nullable = false)
  private UUID servicePointId;

  private String servicePointName;

  @ManyToOne
  @JoinColumn(name = "reading_room_id", nullable = false)
  @ToString.Exclude
  private ReadingRoomEntity readingRoom;

}
