package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.projection.PatronPermissionProjection;
import org.folio.readingroom.exception.PatronPermissionException;
import org.folio.readingroom.repository.PatronPermissionRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.service.converter.Mapper;
import org.folio.readingroom.service.impl.PatronPermissionServiceImpl;
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
  PatronPermissionRepository patronPermissionRepository;
  @Mock
  Mapper mapper;
  @Mock
  UserService userService;

  @InjectMocks
  PatronPermissionServiceImpl patronPermissionsService;

  UUID patronId;
  PatronPermission patronPermission;
  List<PatronPermission> patronPermissions;

  @Mock
  ReadingRoomRepository readingRoomRepository;

  ReadingRoomEntity readingRoomEntity;

  UUID uuid = UUID.randomUUID();

  @BeforeEach
  void initData() {
    patronId = UUID.fromString("7a5abc9f-f3d7-4856-b8d7-6712462ca008");
    patronPermission = new PatronPermission();
    patronPermissions = new ArrayList<>();
    patronPermissions.add(patronPermission);
    readingRoomEntity = HelperUtils.createReadingRoomEntity();
  }

  @Test
  void updatePatronPermissionsByUserId_Success() {
    List<PatronPermissionEntity> patronPermissionEntities = List.of(new PatronPermissionEntity());
    doNothing().when(userService).validatePatronPermissions(any(), any());
    when(mapper.toEntityList(any())).thenReturn(patronPermissionEntities);
    when(patronPermissionRepository.saveAll(patronPermissionEntities)).thenReturn(patronPermissionEntities);
    assertNotNull(patronPermissionsService.updatePatronPermissionsByUserId(UUID.randomUUID(),
                                                                              List.of(new PatronPermission())));
    verify(mapper).toEntityList(any());
    verify(patronPermissionRepository).saveAll(patronPermissionEntities);
  }

  @Test
  void updatePatronPermissionsByUserId_UnMatchedPatronIdAndUserId() {
    UUID differentId = UUID.fromString("7a5abc9f-f3d7-4856-b8d7-6712462ca009");
    patronPermission.setUserId(differentId);
    doThrow(IllegalArgumentException.class).when(userService).validatePatronPermissions(patronId, patronPermissions);
    assertThrows(IllegalArgumentException.class, () -> {
      patronPermissionsService.updatePatronPermissionsByUserId(patronId, patronPermissions);
    });
    verify(userService, times(1)).validatePatronPermissions(patronId, patronPermissions);
  }

  @Test
   void testUpdatePatronPermissionsByUserId_PatronIdNotFound() {
    doThrow(PatronPermissionException.class).when(userService).validatePatronPermissions(patronId, patronPermissions);
    assertThrows(PatronPermissionException.class, () -> {
      patronPermissionsService.updatePatronPermissionsByUserId(patronId, patronPermissions);
    });
    verify(userService, times(1)).validatePatronPermissions(patronId, patronPermissions);
  }

  @Test
  void getPatronPermission_Success() {
    when(readingRoomRepository.findReadingRoomsByUserId(any(UUID.class)))
      .thenReturn(List.of(new PatronPermissionProjection[]{}));
    doNothing().when(userService).validatePatronExistence(any(UUID.class));
    patronPermissionsService.getPatronPermissionsByUserId(UUID.randomUUID(), null);
    verify(readingRoomRepository).findReadingRoomsByUserId(any());
  }

  @Test
  void getPatronPermissionWithEmptyReadingRoom() {
    when(readingRoomRepository.findReadingRoomsByUserIdAndServicePointId(any(UUID.class), any(UUID.class)))
      .thenReturn(List.of());
    doNothing().when(userService).validatePatronExistence(any(UUID.class));
    patronPermissions = patronPermissionsService.getPatronPermissionsByUserId(UUID.randomUUID(), UUID.randomUUID());
    assertEquals(0, patronPermissions.size());
    verify(readingRoomRepository).findReadingRoomsByUserIdAndServicePointId(any(), any());
  }

  @Test
  void getPatronPermission_Failure() {
    doThrow(FeignException.class).when(userService).validatePatronExistence(any(UUID.class));
    assertThrows(FeignException.class, () -> patronPermissionsService
      .getPatronPermissionsByUserId(uuid, uuid));
  }

}
