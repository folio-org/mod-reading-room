package org.folio.readingroom.service;

import org.folio.readingroom.domain.dto.ReadingRoom;

public interface ReadingRoomService {

  /**
   * Creates a new {@code ReadingRoom} instance based on the provided {@code ReadingRoom} object.
   *
   * @param readingRoom - ReadingRoom
   * @return The newly created {@code ReadingRoom} object.
   */
  ReadingRoom createReadingRoom(ReadingRoom readingRoom);
}
