package org.folio.readingroom.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.AccessLogEntity;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;

public class HelperUtils {

  public static final UUID READING_ROOM_ID = UUID.fromString("7a5abc9f-f3d7-4856-b8d7-6712462ca008");
  public static final UUID READING_ROOM_ID_FOR_PATRON_TEST = UUID.fromString("7a5abc9f-f3d7-4856-b8d7-6712462ca009");
  public static final UUID READING_ROOM_ID_FOR_PATRON_TEST_1 = UUID.fromString("8a5abc9f-f3d7-4856-b8d7-6712462ca009");
  public static final UUID SERVICE_POINT_ID1 = UUID.fromString("7c5abc9f-f3d7-4856-b8d7-6712462ca008");
  public static final UUID SERVICE_POINT_ID2 = UUID.fromString("9765c56f-608d-4c93-94fe-f365f16e6970");
  public static final UUID SERVICE_POINT_ID3 = UUID.fromString("9765c56f-608d-4c93-94fe-f365f16e6971");
  public static final UUID SERVICE_POINT_ID4 = UUID.fromString("9865c56f-608d-4c93-94fe-f365f16e6971");
  public static final UUID INVALID_SERVICE_POINT_ID = UUID.fromString("7c5abc9f-f3d7-4856-b8d7-6712462ca009");
  public static final UUID BAD_GATEWAY_SERVICE_POINT_ID = UUID.fromString("7c5abc9f-f3d7-4856-b8d7-6712462ca010");
  public static final UUID ERROR_SERVICE_POINT_ID = UUID.fromString("7c5abc9f-f3d7-4856-b8d7-6712462ca011");

  public static final String READING_ROOM_NAME = "readingRoom1";
  public static final String SERVICE_POINT_NAME1 = "servicePoint1";
  public static final String SERVICE_POINT_NAME2 = "servicePoint2";
  public static final String SERVICE_POINT_NAME3 = "servicePoint3";
  public static final String SERVICE_POINT_NAME4 = "servicePoint4";

  public static ReadingRoom createReadingRoom(UUID readingRoomId, boolean isPublic) {
    ReadingRoom readingRoom = new ReadingRoom();
    readingRoom.setId(readingRoomId);
    readingRoom.setName(READING_ROOM_NAME);
    readingRoom.setIsPublic(isPublic);
    return readingRoom;
  }

  public static PatronPermission createPatronPermission(UUID patronPermissionId, UUID readingRoomId, UUID patronId) {
    PatronPermission patronPermission = new PatronPermission();
    patronPermission.id(patronPermissionId);
    patronPermission.setReadingRoomId(readingRoomId);
    patronPermission.userId(patronId);
    patronPermission.access(PatronPermission.AccessEnum.ALLOWED);
    patronPermission.setNotes("Test Note");
    return patronPermission;
  }

  public static ServicePoint createServicePoint(UUID servicePointId, String servicePointName) {
    ServicePoint servicePoint = new ServicePoint();
    servicePoint.setValue(servicePointId);
    servicePoint.setLabel(servicePointName);
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
    readingRoomEntity.setIsPublic(true);
    Set<ReadingRoomServicePointEntity> servicePoints = new HashSet<>();
    servicePoints.add(createServicePointEntity(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoomEntity.setServicePoints(servicePoints);
    return readingRoomEntity;
  }

  public static AccessLog createAccessLog(UUID accessLogId, AccessLog.ActionEnum actionEnum) {
    AccessLog accessLog = new AccessLog();
    accessLog.setId(accessLogId);
    accessLog.setAction(actionEnum);
    accessLog.setPatronId(UUID.randomUUID());
    accessLog.setUserId(UUID.randomUUID());
    accessLog.readingRoomId(READING_ROOM_ID);
    accessLog.readingRoomName(READING_ROOM_NAME);
    accessLog.servicePointId(SERVICE_POINT_ID1);
    return accessLog;
  }

  public static AccessLogEntity createAccessLogEntity(AccessLog accessLog) {
    AccessLogEntity accessLogEntity = new AccessLogEntity();
    accessLogEntity.setId(accessLog.getId());
    accessLogEntity.setAction(accessLog.getAction());
    accessLogEntity.setUserId(accessLog.getUserId());
    accessLogEntity.setPatronId(accessLog.getPatronId());
    accessLogEntity.setReadingRoomId(READING_ROOM_ID);
    accessLogEntity.setReadingRoomName(READING_ROOM_NAME);
    accessLogEntity.setServicePointId(SERVICE_POINT_ID1);
    return accessLogEntity;
  }
}
