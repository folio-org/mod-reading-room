package org.folio.readingroom.service;

import org.folio.readingroom.domain.dto.Metadata;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.repository.ReadingRoomServicePointRepository;
import org.folio.readingroom.service.converter.ReadingRoomMapper;
import org.folio.readingroom.service.impl.ReadingRoomServiceImpl;
import org.folio.spring.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ReadingRoomServiceTest {
  @Mock
  ReadingRoomRepository readingRoomRepository;
  @Mock
  ReadingRoomMapper readingRoomMapper;
  @Mock
  ReadingRoomServicePointRepository readingRoomServicePointRepository;
  @Mock
  InventoryServicePointService inventoryServicePointService;

  @InjectMocks
  ReadingRoomServiceImpl readingRoomService;

  ReadingRoom readingRoomDto;
  ReadingRoomEntity readingRoomEntity;
  Metadata metadata;
  UUID uuid;

  @BeforeEach
  void initData() {
    uuid = UUID.randomUUID();
    readingRoomDto = new ReadingRoom();
    readingRoomDto.setId(uuid);
    readingRoomDto.setName("rr-1");
    readingRoomDto.setIspublic(true);
    readingRoomDto.setServicePoints(new ArrayList<>());
    metadata = new Metadata();
    metadata.setCreatedDate("2/4/01");
    metadata.setCreatedByUserId(UUID.randomUUID());
    metadata.setUpdatedDate("3/4/02");
    metadata.setUpdatedByUserId(UUID.randomUUID());
    readingRoomDto.setMetadata(metadata);

    readingRoomEntity = new ReadingRoomEntity();
    readingRoomEntity.setId(uuid);
    readingRoomEntity.setName("rr-1");
    readingRoomEntity.setIspublic(true);
    Set<ReadingRoomServicePointEntity> readingRoomServicePointEntities = new HashSet<>();
    readingRoomEntity.setServicePoints(readingRoomServicePointEntities);

  }

  @Test
  void createReadingRoom_Success() {
    Mockito.when(readingRoomMapper.toEntity(Mockito.any(ReadingRoom.class))).thenReturn(readingRoomEntity);
    Mockito.when(readingRoomRepository.save(Mockito.any())).thenReturn(readingRoomEntity);
    Mockito.when(readingRoomMapper.toDto(any(ReadingRoomEntity.class))).thenReturn(readingRoomDto);
    assertEquals(readingRoomService.createReadingRoom(readingRoomDto), readingRoomDto);
    Mockito.verify(readingRoomRepository).save(Mockito.any(ReadingRoomEntity.class));
    Mockito.verify(readingRoomMapper).toEntity(Mockito.any(ReadingRoom.class));
    Mockito.verify(readingRoomMapper).toDto(any(ReadingRoomEntity.class));
  }

  @Test
  void createReadingRoom_ReadingRoomAlreadyExists() {
    Mockito.when(readingRoomRepository.findById(uuid)).thenThrow(ResourceAlreadyExistException.class);
    assertThrows(ResourceAlreadyExistException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    Mockito.verify(readingRoomRepository).findById(uuid);
  }

  @Test
  void createReadingRoom_ServicePointNotFound() {
    Mockito.when(inventoryServicePointService.fetchInvalidServicePointList(any())).thenThrow(NotFoundException.class);
    assertThrows(NotFoundException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    Mockito.verify(inventoryServicePointService).fetchInvalidServicePointList(any());
  }

  @Test
  void createReadingRoom_ServicePointAlreadyAssociated() {
    Mockito.when(readingRoomServicePointRepository.findAllById(any())).thenThrow(ResourceAlreadyExistException.class);
    assertThrows(ResourceAlreadyExistException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    Mockito.verify(readingRoomServicePointRepository).findAllById(any());
  }

  @Test
  void createReadingRoom_DuplicateServicePointIds() {

  }

  @Test
  void createReadingRoom_EmptyServicePointsList() {
//   Mockito.when(readingRoomRepository.save(any())).thenReturn(IllegalArgumentException.class);
//   assertThrows(IllegalArgumentException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
  }

  @Test
  void createReadingRoom_NullReadingRoomId() {

  }
  @Test
  void createReadingRoom_NullServicePointIds() {
  }
}
