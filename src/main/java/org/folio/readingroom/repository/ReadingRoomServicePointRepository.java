package org.folio.readingroom.repository;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRoomServicePointRepository extends JpaRepository<ReadingRoomServicePointEntity, UUID> {
  List<ReadingRoomServicePointEntity> findAllByIdInAndReadingRoomIdNot(Iterable<UUID> uuids, UUID readingRoomId);

}
