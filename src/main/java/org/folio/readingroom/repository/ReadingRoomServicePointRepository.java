package org.folio.readingroom.repository;

import java.util.UUID;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRoomServicePointRepository extends JpaRepository<ReadingRoomServicePointEntity, UUID> {
}
