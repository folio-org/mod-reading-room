package org.folio.readingroom.service;

import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_ID;
import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_ID_FOR_PATRON_TEST;
import static org.folio.readingroom.utils.HelperUtils.createAccessLog;
import static org.folio.readingroom.utils.HelperUtils.createAccessLogEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.AccessLogCollection;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ReadingRoomCollection;
import org.folio.readingroom.domain.entity.AccessLogEntity;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.folio.readingroom.exception.IdMismatchException;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.exception.ServicePointException;
import org.folio.readingroom.repository.AccessLogRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.repository.ReadingRoomServicePointRepository;
import org.folio.readingroom.service.converter.Mapper;
import org.folio.readingroom.service.impl.ReadingRoomServiceImpl;
import org.folio.readingroom.utils.HelperUtils;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ReadingRoomServiceTest {

  @Mock
  ReadingRoomRepository readingRoomRepository;

  @Mock
  Mapper readingRoomMapper;

  @Mock
  ReadingRoomServicePointRepository readingRoomServicePointRepository;

  @Mock
  AccessLogRepository accessLogRepository;

  @Mock
  ServicePointService servicePointService;

  @InjectMocks
  ReadingRoomServiceImpl readingRoomService;

  ReadingRoom readingRoomDto;
  ReadingRoomEntity readingRoomEntity;
  ReadingRoomEntity readingRoomEntity1;
  ReadingRoomEntity readingRoomEntity2;
  UUID uuid;
  String query;
  Integer offset;
  Integer limit;

  @BeforeEach
  void initData() {
    uuid = UUID.randomUUID();
    readingRoomEntity = HelperUtils.createReadingRoomEntity();
    readingRoomDto = HelperUtils.createReadingRoom(uuid, true);
    query = "some query";
    offset = 0;
    limit = 10;
    readingRoomEntity1 = HelperUtils.createReadingRoomEntity();
    readingRoomEntity1.setDeleted(true);
    readingRoomEntity2 = HelperUtils.createReadingRoomEntity();
    readingRoomEntity2.setDeleted(false);
  }

  @Test
  void createReadingRoom_Success() {
    when(readingRoomMapper.toEntity(any(ReadingRoom.class))).thenReturn(readingRoomEntity);
    when(readingRoomRepository.save(any())).thenReturn(readingRoomEntity);
    when(readingRoomMapper.toDto(any(ReadingRoomEntity.class))).thenReturn(readingRoomDto);
    assertEquals(readingRoomService.createReadingRoom(readingRoomDto), readingRoomDto);
    verify(readingRoomRepository).save(any(ReadingRoomEntity.class));
    verify(readingRoomMapper).toEntity(any(ReadingRoom.class));
    verify(readingRoomMapper).toDto(any(ReadingRoomEntity.class));
  }

  @Test
  void createReadingRoom_ReadingRoomAlreadyExists() {
    when(readingRoomRepository.findById(any())).thenReturn(Optional.of(readingRoomEntity));
    assertThrows(ResourceAlreadyExistException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    verify(readingRoomRepository).findById(uuid);
  }

  @Test
  void createReadingRoom_ServicePointNotFound() {
    var invalidServicePointsList = List.of(UUID.randomUUID());
    when(servicePointService.fetchInvalidServicePointList(any())).thenReturn(invalidServicePointsList);
    assertThrows(ServicePointException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    verify(servicePointService).fetchInvalidServicePointList(any());

  }

  @Test
  void createReadingRoom_ServicePointAlreadyAssociated() {
    var readingRoomServicePointEntity = new ReadingRoomServicePointEntity();
    List<ReadingRoomServicePointEntity> existingServicePointList = List.of(readingRoomServicePointEntity);
    when(readingRoomServicePointRepository.findAllById(any())).thenReturn(existingServicePointList);
    assertThrows(ServicePointException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    verify(readingRoomServicePointRepository).findAllById(any());
  }

  @Test
  void deleteReadingRoomById_Success() {
    when(readingRoomRepository.findById(READING_ROOM_ID)).thenReturn(Optional.of(readingRoomEntity));
    readingRoomService.deleteReadingRoomById(READING_ROOM_ID);
    assertTrue(readingRoomEntity.isDeleted());
    verify(readingRoomRepository).findById(READING_ROOM_ID);
  }

  @Test
  void deleteReadingRoomById_ReadingRoomNotFound() {
    when(readingRoomRepository.findById(READING_ROOM_ID)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> readingRoomService.deleteReadingRoomById(READING_ROOM_ID));
  }

  @Test
  void getReadingRoomsByCqlQuery_NotIncludeDeleted() {
    var includeDeleted = false;
    List<ReadingRoomEntity> readingRoomEntities = List.of(readingRoomEntity1, readingRoomEntity2);
    Page<ReadingRoomEntity> page = new PageImpl<>(readingRoomEntities, PageRequest.of(offset, limit),
                                                                        readingRoomEntities.size());
    when(readingRoomRepository.findByCql(query, OffsetRequest.of(offset, limit))).thenReturn(page);
    when(readingRoomMapper.toDto(page, includeDeleted)).thenReturn(
                                  new ReadingRoomCollection(List.of(new ReadingRoom()), 1));
    ReadingRoomCollection result = readingRoomService.getReadingRoomsByCqlQuery(query, offset, limit, includeDeleted);
    assertEquals(1, result.getTotalRecords());
    verify(readingRoomRepository).findByCql(query, OffsetRequest.of(offset, limit));
    verify(readingRoomMapper).toDto(page, includeDeleted);
  }

  @Test
  void getReadingRoomsByCqlQuery_IncludeDeleted() {
    var includeDeleted = true;
    List<ReadingRoomEntity> readingRoomEntities = List.of(readingRoomEntity1, readingRoomEntity2);
    Page<ReadingRoomEntity> page = new PageImpl<>(readingRoomEntities, PageRequest.of(offset, limit),
      readingRoomEntities.size());
    when(readingRoomRepository.findByCql(query, OffsetRequest.of(offset, limit))).thenReturn(page);
    when(readingRoomMapper.toDto(page, includeDeleted)).thenReturn(new ReadingRoomCollection(
      List.of(new ReadingRoom(), new ReadingRoom()), 2));
    ReadingRoomCollection result = readingRoomService.getReadingRoomsByCqlQuery(query, offset, limit, includeDeleted);
    assertEquals(2, result.getTotalRecords());
    verify(readingRoomRepository).findByCql(query, OffsetRequest.of(offset, limit));
    verify(readingRoomMapper).toDto(page, includeDeleted);
  }

  @Test
  void updateReadingRoom_ReadingRoomIdMismatch() {
    assertThrows(IdMismatchException.class,
      () -> readingRoomService.updateReadingRoom(READING_ROOM_ID, readingRoomDto));
  }

  @Test
  void updateReadingRoom_ReadingRoomNotFound() {
    when(readingRoomRepository.findById(any())).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> readingRoomService.updateReadingRoom(uuid, readingRoomDto));
    verify(readingRoomRepository).findById(any());
  }

  @Test
  void updateReadingRoom_ServicePointAlreadyAssociatedButNotWithUpdatingReadingRoom() {
    var existingServicePointList = List.of(new ReadingRoomServicePointEntity());
    when(readingRoomRepository.findById(any())).thenReturn(Optional.of(readingRoomEntity));
    when(readingRoomServicePointRepository.findAllByIdInAndReadingRoomIdNot(any(), any()))
      .thenReturn(existingServicePointList);
    assertThrows(ServicePointException.class,
      () -> readingRoomService.updateReadingRoom(uuid, readingRoomDto));
    verify(readingRoomRepository).findById(any());
    verify(readingRoomServicePointRepository).findAllByIdInAndReadingRoomIdNot(any(), any());
  }

  @Test
  void updateReadingRoom_Success() {
    when(readingRoomRepository.findById(any())).thenReturn(Optional.of(readingRoomEntity));
    when(readingRoomMapper.toEntity(any(ReadingRoom.class))).thenReturn(readingRoomEntity);
    when(readingRoomRepository.save(any())).thenReturn(readingRoomEntity);
    when(readingRoomMapper.toDto(any(ReadingRoomEntity.class))).thenReturn(readingRoomDto);
    assertNotNull(readingRoomService.updateReadingRoom(uuid, readingRoomDto));
    verify(readingRoomRepository).findById(any());
    verify(readingRoomMapper).toEntity(any(ReadingRoom.class));
    verify(readingRoomRepository).save(any());
    verify(readingRoomMapper).toDto(any(ReadingRoomEntity.class));
  }

  @Test
  void createAccessLog_success() {
    var accessLog = createAccessLog(READING_ROOM_ID, AccessLog.ActionEnum.ALLOWED);
    var accessLogEntity = createAccessLogEntity(accessLog);
    when(accessLogRepository.save(any())).thenReturn(accessLogEntity);
    when(accessLogRepository.findById(any())).thenReturn(Optional.empty());
    when(readingRoomMapper.toDto(accessLogEntity)).thenReturn(accessLog);
    assertEquals(readingRoomService.createAccessLog(READING_ROOM_ID, accessLog), accessLog);
  }

  @Test
  void createAccessLog_MismatchReadingRoomId() {
    var accessLog = createAccessLog(READING_ROOM_ID, AccessLog.ActionEnum.ALLOWED);
    assertThrows(IdMismatchException.class, () ->
      readingRoomService.createAccessLog(READING_ROOM_ID_FOR_PATRON_TEST, accessLog));
  }

  @Test
  void createAccessLog_AccessLogAlreadyExists() {
    var accessLog = createAccessLog(READING_ROOM_ID, AccessLog.ActionEnum.ALLOWED);
    var accessLogEntity = createAccessLogEntity(accessLog);
    when(accessLogRepository.findById(any())).thenReturn(Optional.of(accessLogEntity));
    assertThrows(ResourceAlreadyExistException.class, () ->
      readingRoomService.createAccessLog(READING_ROOM_ID, accessLog));
  }

  @Test
  void getAccessLogWithValidCqlQuery() {
    var accessLog1 = createAccessLog(READING_ROOM_ID, AccessLog.ActionEnum.ALLOWED);
    var accessLogEntity1 = createAccessLogEntity(accessLog1);
    var accessLogEntities = List.of(accessLogEntity1);
    Page<AccessLogEntity> accessLogEntityPage = new PageImpl<>(accessLogEntities, PageRequest.of(offset, limit),
      accessLogEntities.size());
    when(accessLogRepository.findByCql(eq("cql.allRecords=1"), any()))
      .thenReturn(accessLogEntityPage);
    when(readingRoomMapper.toDtoCollection(accessLogEntityPage))
      .thenReturn(new AccessLogCollection(List.of(accessLog1), 1));
    var response = readingRoomService.getAccessLogsByCqlQuery("cql.allRecords=1", 0, 10);
    verify(accessLogRepository).findByCql(any(), any());
    verify(readingRoomMapper).toDtoCollection(accessLogEntityPage);
    assertEquals(1, response.getTotalRecords());
    assertEquals(accessLog1, response.getAccessLogs().get(0));
  }

  @Test
  void getAccessLogWithInvalidValueInCqlQuery() {
    var randomReadingRoomId = UUID.randomUUID();
    Page<AccessLogEntity> accessLogEntityPage = new PageImpl<>(List.of(), PageRequest.of(offset, limit), 1);
    when(accessLogRepository.findByCql(eq("readingRoomId=" + randomReadingRoomId), any()))
      .thenReturn(accessLogEntityPage);
    when(readingRoomMapper.toDtoCollection(accessLogEntityPage))
      .thenReturn(new AccessLogCollection(List.of(), 0));
    var response = readingRoomService.getAccessLogsByCqlQuery("readingRoomId=" + randomReadingRoomId, 0, 10);
    verify(accessLogRepository).findByCql(any(), any());
    verify(readingRoomMapper).toDtoCollection(accessLogEntityPage);
    assertEquals(0, response.getTotalRecords());
    assertEquals(0, response.getAccessLogs().size());
  }
}
