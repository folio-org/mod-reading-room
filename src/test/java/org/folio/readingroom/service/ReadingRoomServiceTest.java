package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import java.util.UUID;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.repository.ReadingRoomServicePointRepository;
import org.folio.readingroom.service.converter.ReadingRoomMapper;
import org.folio.readingroom.service.impl.ReadingRoomServiceImpl;
import org.folio.readingroom.utils.HelperUtils;
import org.folio.spring.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

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
  UUID uuid;

  @BeforeEach
  void initData() {
    uuid = UUID.randomUUID();
    readingRoomEntity = HelperUtils.createReadingRoomEntity();
    readingRoomDto = HelperUtils.createReadingRoom(uuid, true);
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
    Mockito.when(readingRoomRepository.findById(any())).thenThrow(ResourceAlreadyExistException.class);
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
    // Write your test case for this scenario
  }

  @Test
  void createReadingRoom_NullReadingRoomId() {
    // Write your test case for this scenario
  }

  @Test
  void createReadingRoom_NullServicePointIds() {
    // Write your test case for this scenario
  }
}
