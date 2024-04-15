package org.folio.readingroom.service.impl;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.repository.PatonPermissionsRepository;
import org.folio.readingroom.service.PatronPermissionsService;
import org.folio.readingroom.service.UserService;
import org.folio.readingroom.service.converter.Mapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class PatronPermissionsServiceImpl implements PatronPermissionsService {
  private final PatonPermissionsRepository patonPermissionsRepository;
  private final Mapper mapper;
  private final UserService userService;

  @Override
  public List<PatronPermission> updatePatronPermissionsByUserId(UUID patronId,
    List<PatronPermission> patronPermissions) {
    userService.validatePatron(patronId, patronPermissions);
    List<PatronPermissionEntity> entities = mapper.toEntityList(patronPermissions);
    patonPermissionsRepository.saveAll(entities);
    return patronPermissions;
  }
}
