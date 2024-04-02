package org.folio.readingroom.repository;

import java.util.UUID;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRoomRepository extends JpaRepository<ReadingRoomEntity, UUID> {
}
