package org.folio.readingroom.utils;

import java.util.UUID;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ServicePoint;

public class HelperUtils {

  public static final UUID READING_ROOM_ID = UUID.fromString("7a5abc9f-f3d7-4856-b8d7-6712462ca008");

  public static final UUID SERVICE_POINT_ID1 = UUID.fromString("7c5abc9f-f3d7-4856-b8d7-6712462ca008");
  public static final UUID SERVICE_POINT_ID2 = UUID.fromString("9765c56f-608d-4c93-94fe-f365f16e6970");

  public static ReadingRoom createReadingRoom(UUID readingRoomId) {
    return ReadingRoom.builder()
      .id(readingRoomId)
      .name("readingRoom1")
      .ispublic(true)
      .build();
  }

  public static ServicePoint createServicePoint(UUID servicePointId) {
    return ServicePoint.builder()
      .id(servicePointId)
      .name("Service point 1")
      .build();
  }
}
