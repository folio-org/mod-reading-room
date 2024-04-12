package org.folio.readingroom.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;

public class HelperUtils {

  public static final UUID READING_ROOM_ID = UUID.fromString("7a5abc9f-f3d7-4856-b8d7-6712462ca008");

  public static final UUID SERVICE_POINT_ID1 = UUID.fromString("7c5abc9f-f3d7-4856-b8d7-6712462ca008");
  public static final UUID SERVICE_POINT_ID2 = UUID.fromString("9765c56f-608d-4c93-94fe-f365f16e6970");
  public static final UUID SERVICE_POINT_ID3 = UUID.fromString("9765c56f-608d-4c93-94fe-f365f16e6971");
  public static final UUID INVALID_SERVICE_POINT_ID = UUID.fromString("7c5abc9f-f3d7-4856-b8d7-6712462ca009");

  public static final String READING_ROOM_NAME = "readingRoom1";
  public static final String SERVICE_POINT_NAME1 = "servicePoint1";
  public static final String SERVICE_POINT_NAME2 = "servicePoint2";

  public static ReadingRoom createReadingRoom(UUID readingRoomId, boolean ispublic) {
    ReadingRoom readingRoom = new ReadingRoom();
    readingRoom.setId(readingRoomId);
    readingRoom.setName(READING_ROOM_NAME);
    readingRoom.setIspublic(ispublic);
    return readingRoom;
  }

  public static ServicePoint createServicePoint(UUID servicePointId, String servicePointName) {
    ServicePoint servicePoint = new ServicePoint();
    servicePoint.setId(servicePointId);
    servicePoint.setName(servicePointName);
    return servicePoint;
  }

  public static ReadingRoomServicePointEntity createServicePointEntity(UUID servicePointId, String servicePointName) {
    ReadingRoomServicePointEntity servicePointEntity = new ReadingRoomServicePointEntity();
    servicePointEntity.setId(servicePointId);
    servicePointEntity.setName(servicePointName);
    return servicePointEntity;
  }

  public static ReadingRoomEntity createReadingRoomEntity() {
    ReadingRoomEntity readingRoomEntity = new ReadingRoomEntity();
    readingRoomEntity.setId(READING_ROOM_ID);
    readingRoomEntity.setName(READING_ROOM_NAME);
    readingRoomEntity.setIspublic(true);
    Set<ReadingRoomServicePointEntity> servicePoints = new HashSet<>();
    servicePoints.add(createServicePointEntity(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoomEntity.setServicePoints(servicePoints);
    return readingRoomEntity;
  }
}
