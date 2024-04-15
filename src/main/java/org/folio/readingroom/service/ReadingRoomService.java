package org.folio.readingroom.service;

import java.util.UUID;
import org.folio.readingroom.domain.dto.ReadingRoom;

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


  void deleteReadingRoomById(UUID readingRoomId);
}
