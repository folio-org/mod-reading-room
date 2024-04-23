package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.service.impl.PatronPermissionsServiceImpl;
import org.folio.readingroom.utils.HelperUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatronPermissionServiceTest {
  @Mock
  ReadingRoomRepository readingRoomRepository;
  @Mock
  UserService userService;
  @InjectMocks
  PatronPermissionsServiceImpl patronPermissionsService;
  ReadingRoomEntity readingRoomEntity;
  UUID uuid = UUID.randomUUID();

  @BeforeEach
  void initData() {
    readingRoomEntity = HelperUtils.createReadingRoomEntity();
  }

  @Test
  void getPatronPermission_Success() {
    when(readingRoomRepository.findReadingRoomsByUserId(any(UUID.class)))
      .thenReturn(Collections.singletonList(readingRoomEntity));
    doNothing().when(userService).validatePatronExistence(any(UUID.class));
    patronPermissionsService.getPatronPermissionsByUserId(UUID.randomUUID(), null);
    verify(readingRoomRepository).findReadingRoomsByUserId(any());
  }

  @Test
  void getPatronPermissionWithEmptyReadingRoom() {
    when(readingRoomRepository.findReadingRoomsByUserIdAndServicePointId(any(UUID.class), any(UUID.class)))
      .thenReturn(null);
    doNothing().when(userService).validatePatronExistence(any(UUID.class));
    List<PatronPermission> patronPermissions =
      patronPermissionsService.getPatronPermissionsByUserId(UUID.randomUUID(), UUID.randomUUID());
    assertEquals(1, patronPermissions.size());
    verify(readingRoomRepository).findReadingRoomsByUserIdAndServicePointId(any(), any());
  }

  @Test
  void getPatronPermission_Failure() {
    doThrow(FeignException.class).when(userService).validatePatronExistence(any(UUID.class));
    assertThrows(FeignException.class, () -> patronPermissionsService
      .getPatronPermissionsByUserId(uuid, uuid));
  }
}
