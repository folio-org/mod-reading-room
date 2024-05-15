package org.folio.readingroom.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.domain.projection.PatronPermissionProjection;
import org.folio.readingroom.repository.PatronPermissionRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.service.PatronPermissionService;
import org.folio.readingroom.service.UserService;
import org.folio.readingroom.service.converter.Mapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class PatronPermissionServiceImpl implements PatronPermissionService {
  private final PatronPermissionRepository patronPermissionRepository;
  private final ReadingRoomRepository readingRoomRepository;
  private final Mapper mapper;
  private final UserService userService;

  @Override
  public List<PatronPermission> updatePatronPermissionsByUserId(UUID patronId,
                                                                List<PatronPermission> patronPermissions) {
    userService.validatePatronPermissions(patronId, patronPermissions);
    List<PatronPermissionEntity> entities = mapper.toEntityList(patronPermissions);
    patronPermissionRepository.saveAll(entities);
    return patronPermissions;
  }

  @Override
  public List<PatronPermission> getPatronPermissionsByUserId(UUID patronId, UUID servicePointId) {
    userService.validatePatronExistence(patronId);
    List<PatronPermissionProjection> patronPermissions = Objects.nonNull(servicePointId)
      ? readingRoomRepository.findReadingRoomsByUserIdAndServicePointId(patronId, servicePointId)
      : readingRoomRepository.findReadingRoomsByUserId(patronId);
    return patronPermissions.isEmpty()
      ? Collections.emptyList()
      : patronPermissions.stream().map(mapper::toDto).toList();
  }

}
