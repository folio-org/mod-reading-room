package org.folio.readingroom.domain.projection;

import java.util.UUID;

public interface PatronPermissionProjection {

  UUID getId();

  UUID getUserId();

  UUID getReadingRoomId();

  String getReadingRoomName();

  String getAccess();

  String getNotes();

  UUID getCreatedBy();

  String getCreatedDate();

  UUID getUpdatedBy();

  String getUpdatedDate();

}
