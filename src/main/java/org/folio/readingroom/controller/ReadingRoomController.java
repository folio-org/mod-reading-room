package org.folio.readingroom.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ReadingRoomCollection;
import org.folio.readingroom.rest.resource.ReadingRoomApi;
import org.folio.readingroom.service.ReadingRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Log4j2
public class ReadingRoomController implements ReadingRoomApi {

  private final ReadingRoomService readingRoomService;

  @Override
  public ResponseEntity<ReadingRoom> createReadingRoom(ReadingRoom readingRoom) {
    log.info("createReadingRoom:: creating reading room with id {}, name {}",
      readingRoom.getId(), readingRoom.getName());
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(readingRoomService.createReadingRoom(readingRoom));
  }

  @Override
  public ResponseEntity<ReadingRoom> updateReadingRoomById(UUID readingRoomId, ReadingRoom readingRoom) {
    log.info("updateReadingRoomById:: updating reading room with id {}, name {}",
      readingRoom.getId(), readingRoom.getName());
    return ResponseEntity.status(HttpStatus.OK)
      .body(readingRoomService.updateReadingRoom(readingRoomId, readingRoom));
  }

  @Override
  public ResponseEntity<ReadingRoomCollection> getReadingRoomsByCqlQuery(String query, Integer offset,
                                                                         Integer limit, Boolean includeDeleted) {
    log.info("getReadingRoomsByCqlQuery:: fetch reading room list by cql query {}, "
      + "offset {}, limit {}, includeDeleted {}", query, offset, limit, includeDeleted);
    return ResponseEntity.status(HttpStatus.OK)
      .body(readingRoomService.getReadingRoomsByCqlQuery(query, offset, limit, includeDeleted));
  }

}
