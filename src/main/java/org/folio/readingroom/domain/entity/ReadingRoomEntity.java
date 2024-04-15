package org.folio.readingroom.domain.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.folio.readingroom.domain.base.AuditableEntity;

@Entity
@Getter
@Setter
@ToString(exclude = {"servicePoints"})
@Table(name = "reading_room")
public class ReadingRoomEntity extends AuditableEntity {

  @Id
  @Column(nullable = false)
  private UUID id;

  private String name;

  private boolean ispublic;

  @OneToMany(cascade = CascadeType.ALL,
    fetch = FetchType.LAZY,
    mappedBy = "readingRoom",
    orphanRemoval = true)
  private Set<ReadingRoomServicePointEntity> servicePoints = new LinkedHashSet<>();

  public void removeServicePoints(ReadingRoomServicePointEntity readingRoomServicePointEntity) {
    readingRoomServicePointEntity.setReadingRoom(null);
    this.servicePoints.remove(readingRoomServicePointEntity);
  }

  public void addServicePoints(ReadingRoomServicePointEntity readingRoomServicePointEntity) {
    readingRoomServicePointEntity.setReadingRoom(this);
    this.servicePoints.add(readingRoomServicePointEntity);
  }
}
