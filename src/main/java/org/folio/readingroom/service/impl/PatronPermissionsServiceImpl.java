package org.folio.readingroom.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.Metadata;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.repository.PatronPermissionsRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.service.PatronPermissionsService;
import org.folio.readingroom.service.UserService;
import org.folio.readingroom.service.converter.Mapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class PatronPermissionsServiceImpl implements PatronPermissionsService {
  private final PatronPermissionsRepository patronPermissionsRepository;
  private final ReadingRoomRepository readingRoomRepository;
  private final Mapper mapper;
  private final UserService userService;

  @Override
  public List<PatronPermission> updatePatronPermissionsByUserId(UUID patronId,
                                                                List<PatronPermission> patronPermissions) {
    userService.validatePatronPermissions(patronId, patronPermissions);
    List<PatronPermissionEntity> entities = mapper.toEntityList(patronPermissions);
    patronPermissionsRepository.saveAll(entities);
    return patronPermissions;
  }

  @Override
  public List<PatronPermission> getPatronPermissionsByUserId(UUID patronId, UUID servicePointId) {
    userService.validatePatronExistence(patronId);
    if (Objects.nonNull(servicePointId)) {
      ReadingRoomEntity readingRoom = readingRoomRepository
        .findReadingRoomsByUserIdAndServicePointId(patronId, servicePointId);
      return Collections.singletonList(convertReadingRoomToPatronPermission(readingRoom, patronId));
    } else {
      List<ReadingRoomEntity> readingRoomEntities = readingRoomRepository.findReadingRoomsByUserId(patronId);
      return readingRoomEntities.stream()
        .map(readingRoomEntity -> convertReadingRoomToPatronPermission(readingRoomEntity, patronId))
        .toList();
    }
  }

  private PatronPermission convertReadingRoomToPatronPermission(ReadingRoomEntity readingRoomEntity, UUID patronId) {
    PatronPermission patronPermission = new PatronPermission();
    Metadata metadata = new Metadata();
    if (Objects.nonNull(readingRoomEntity)) {
      patronPermission.setUserId(patronId);
      patronPermission.setReadingRoomId(readingRoomEntity.getId());
      patronPermission.setReadingRoomName(readingRoomEntity.getName());

      Optional<PatronPermissionEntity> optionalPermission = readingRoomEntity
        .getPatronPermissions().stream().findFirst();

      if (optionalPermission.isPresent()) {
        PatronPermissionEntity permission = optionalPermission.get();
        patronPermission.setId(permission.getId());
        patronPermission.setNotes(permission.getNotes());
        patronPermission.setAccess(permission.getAccess());
        metadata.setCreatedByUserId(permission.getCreatedBy());
        metadata.setCreatedDate(permission.getCreatedDate().toString());
        metadata.setUpdatedByUserId(permission.getUpdatedBy() != null ? permission.getUpdatedBy() : null);
        metadata.setUpdatedDate(permission.getUpdatedDate() != null ? permission.getUpdatedDate().toString() : null);
        patronPermission.setMetadata(metadata);
      } else {
        if (Boolean.TRUE.equals(readingRoomEntity.getIsPublic())) {
          patronPermission.setAccess(PatronPermission.AccessEnum.ALLOWED);
        } else {
          patronPermission.setAccess(PatronPermission.AccessEnum.NOT_ALLOWED);
        }
      }
    }
    return patronPermission;
  }
}
