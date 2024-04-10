package org.folio.readingroom.repository;

import java.util.UUID;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRoomRepository extends JpaCqlRepository<ReadingRoomEntity, UUID> {
}
