package org.folio.readingroom.repository;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.projection.PatronPermissionProjection;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReadingRoomRepository extends JpaCqlRepository<ReadingRoomEntity, UUID> {

  @Query(value = """
      SELECT p.id, COALESCE(p.user_id, ?1) as userId, r.id as readingRoomId, r.name as readingRoomName,
             CASE
                 WHEN p.access IS NULL THEN CASE
                                                WHEN r.is_public = TRUE THEN 'ALLOWED'
                                                ELSE 'NOT_ALLOWED'
                                            END
                 ELSE CAST(p.access AS TEXT)
             END AS access,
             p.notes, p.created_by as createdBy, TO_CHAR(p.created_date, 'YYYY-MM-DD HH24:MI:SS') AS createdDate,
             p.updated_by as updatedBy, TO_CHAR(p.updated_date, 'YYYY-MM-DD HH24:MI:SS') AS updatedDate
      FROM reading_room r
           LEFT JOIN patron_Permission p ON r.id = p.reading_room_id AND p.user_Id = ?1
      WHERE r.is_Deleted = FALSE
    """, nativeQuery = true)
  List<PatronPermissionProjection> findReadingRoomsByUserId(@Param("userId") UUID userId);

  @Query(value = """
      SELECT p.id, COALESCE(p.user_id, ?1) as userId, r.id as readingRoomId, r.name as readingRoomName,
             CASE
                 WHEN p.access IS NULL THEN CASE
                                                WHEN r.is_public = TRUE THEN 'ALLOWED'
                                                ELSE 'NOT_ALLOWED'
                                            END
                 ELSE CAST(p.access AS TEXT)
             END AS access,
             p.notes, p.created_by as createdBy, TO_CHAR(p.created_date, 'YYYY-MM-DD HH24:MI:SS') AS createdDate,
             p.updated_by as updatedBy, TO_CHAR(p.updated_date, 'YYYY-MM-DD HH24:MI:SS') AS updatedDate
      FROM
        reading_room r
        LEFT JOIN patron_Permission p ON r.id = p.reading_room_id AND p.user_Id = ?1
        LEFT JOIN reading_room_service_point rs ON r.id = rs.reading_room_id
      WHERE r.is_Deleted = FALSE and rs.service_point_id = ?2
    """, nativeQuery = true)
  List<PatronPermissionProjection> findReadingRoomsByUserIdAndServicePointId(@Param("userId") UUID userId,
    @Param("servicePointId") UUID servicePointId);
}
