package org.folio.readingroom.repository;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRoomRepository extends JpaCqlRepository<ReadingRoomEntity, UUID> {
  @Query(value = """
      SELECT r
        FROM ReadingRoomEntity r
        LEFT JOIN fetch r.patronPermissions p
        WHERE (p.userId = :userId OR p.userId IS NULL)
        AND r.isDeleted = false
    """)
  List<ReadingRoomEntity> findReadingRoomsByUserId(@Param("userId") UUID userId);

  @Query(value = """
      SELECT r
        FROM ReadingRoomEntity r
        LEFT JOIN FETCH r.patronPermissions p
        LEFT JOIN FETCH r.servicePoints s
        WHERE (p.userId = :userId OR p.userId IS NULL)
        AND s.id = :servicePointId
        AND r.isDeleted = false
    """)
  ReadingRoomEntity findReadingRoomsByUserIdAndServicePointId(@Param("userId") UUID userId,
    @Param("servicePointId") UUID servicePointId);
}
