package org.folio.readingroom.controller;

import lombok.AllArgsConstructor;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.rest.resource.ReadingRoomApi;
import org.folio.readingroom.service.ReadingRoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ReadingRoomController implements ReadingRoomApi {

  private final ReadingRoomService readingRoomService;

  @Override
  public ResponseEntity<ReadingRoom> createReadingRoom(ReadingRoom readingRoom) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(readingRoomService.createReadingRoom(readingRoom));
  }

}
