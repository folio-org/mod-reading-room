package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.exception.ServicePointException;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.repository.ReadingRoomServicePointRepository;
import org.folio.readingroom.service.converter.ReadingRoomMapper;
import org.folio.readingroom.service.impl.ReadingRoomServiceImpl;
import org.folio.readingroom.utils.HelperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
  ServicePointService servicePointService;

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
    when(servicePointService.fetchInvalidServicePointList(any())).thenThrow(ServicePointException.class);
    assertThrows(ServicePointException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    verify(servicePointService).fetchInvalidServicePointList(any());

  }

  @Test
  void createReadingRoom_ServicePointAlreadyAssociated() {
    when(readingRoomServicePointRepository.findAllById(any())).thenThrow(ServicePointException.class);
    assertThrows(ServicePointException.class, () -> readingRoomService.createReadingRoom(readingRoomDto));
    verify(readingRoomServicePointRepository).findAllById(any());
  }

}
