package org.folio.readingroom.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.service.PatronPermissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
public class PatronPermissionController implements org.folio.readingroom.rest.resource.ReadingRoomPatronPermissionApi {
  private final PatronPermissionService patronPermissionService;

  @Override
  public ResponseEntity<List<PatronPermission>> getPatronPermissionsByUserId(UUID patronId, UUID servicePointId) {
    log.info("getPatronPermissionsByUserId:: getting patronPermission for patron {}", patronId);
    return ResponseEntity.status(HttpStatus.OK)
      .body(patronPermissionService.getPatronPermissionsByUserId(patronId, servicePointId));
  }

  @Override
  public ResponseEntity<List<PatronPermission>> updatePatronReadingRoomPermission(UUID patronId,
    List<PatronPermission> patronPermission) {
    log.info("updatePatronPermissionsByUserId:: Updating patronPermission for patron {}", patronId);
    return ResponseEntity.status(HttpStatus.OK)
      .body(patronPermissionService
        .updatePatronPermissionsByUserId(patronId, patronPermission));
  }
}
