package org.folio.readingroom.service;

import java.util.UUID;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.AccessLogCollection;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ReadingRoomCollection;

public interface ReadingRoomService {

  /**
   * Creates a new {@code ReadingRoom} instance based on the provided {@code ReadingRoom} object.
   *
   * @param readingRoom - ReadingRoom
   * @return The newly created {@code ReadingRoom} object.
   */
  ReadingRoom createReadingRoom(ReadingRoom readingRoom);

  /**
   * update existing {@code ReadingRoom} entity based on the provided {@code ReadingRoom} object.
   *
   * @param readingRoomId - id of the reading room
   * @param readingRoom - ReadingRoom
   * @return The newly updated {@code ReadingRoom} object.
   */
  ReadingRoom updateReadingRoom(UUID readingRoomId, ReadingRoom readingRoom);

  /**
   * Retrieves a collection of reading rooms based on the given CQL.
   *
   * @param query  - The CQL query used to filter the reading rooms.
   * @param offset - The offset for pagination, indicating the starting index of the result set.
   * @param limit -  The maximum number of reading rooms to retrieve.
   * @param includeDeleted - Flag to include the deleted records
   * @return A ReadingRoomCollection {@code ReadingRoomCollection} that match the specified CQL query.
   */
  ReadingRoomCollection getReadingRoomsByCqlQuery(String query, Integer offset, Integer limit, Boolean includeDeleted);

  /**
   * soft delete existing {@code ReadingRoom} entity based on  provided reading room id.
   *
   * @param readingRoomId - id of the reading room
   */
  void deleteReadingRoomById(UUID readingRoomId);

  /**
   * Creates an access log entry for the specified reading room.
   *
   * @param readingRoomId The UUID of the reading room for which the access log is created.
   * @param accessLog The access log entry to be created.
   * @return The created access log entry.
   */
  AccessLog createAccessLog(UUID readingRoomId, AccessLog accessLog);

  /**
   * Retrieves a collection of access log entries based on the given CQL.
   *
   * @param query  - The CQL query used to filter the access logs.
   * @param offset - The offset for pagination, indicating the starting index of the result set.
   * @param limit -  The maximum number of access log entries to retrieve.
   * @return A AccessLogCollection {@code AccessLogCollection} that match the specified CQL query.
   */
  AccessLogCollection getAccessLogsByCqlQuery(String query, Integer offset, Integer limit);

}
