package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.exception.PatronPermissionsException;
import org.folio.readingroom.repository.PatronPermissionsRepository;
import org.folio.readingroom.service.converter.Mapper;
import org.folio.readingroom.service.impl.PatronPermissionsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
 class PatronPermissionsServiceTest {
  @Mock
  PatronPermissionsRepository patronPermissionsRepository;
  @Mock
  Mapper mapper;
  @Mock
  UserService userService;

  @InjectMocks
  PatronPermissionsServiceImpl patronPermissionsService;

  UUID patronId;
  PatronPermission patronPermission;
  List<PatronPermission> patronPermissions;

  @BeforeEach
  void initData() {
    patronId = UUID.fromString("7a5abc9f-f3d7-4856-b8d7-6712462ca008");
    patronPermission = new PatronPermission();
    patronPermissions = new ArrayList<>();
    patronPermissions.add(patronPermission);
  }

  @Test
  void updatePatronPermissionsByUserId_Success() {
    List<PatronPermissionEntity> patronPermissionEntities = List.of(new PatronPermissionEntity());
    doNothing().when(userService).validatePatronPermissions(any(), any());
    when(mapper.toEntityList(any())).thenReturn(patronPermissionEntities);
    when(patronPermissionsRepository.saveAll(patronPermissionEntities)).thenReturn(patronPermissionEntities);
    assertNotNull(patronPermissionsService.updatePatronPermissionsByUserId(UUID.randomUUID(),
                                                                              List.of(new PatronPermission())));
    verify(mapper).toEntityList(any());
    verify(patronPermissionsRepository).saveAll(patronPermissionEntities);
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

    List<PatronPermission> patronPermissions = new ArrayList<>();
    doThrow(PatronPermissionsException.class).when(userService).validatePatronPermissions(patronId, patronPermissions);
    assertThrows(PatronPermissionsException.class, () -> {
      patronPermissionsService.updatePatronPermissionsByUserId(patronId, patronPermissions);
    });
    verify(userService, times(1)).validatePatronPermissions(patronId, patronPermissions);
  }


}
