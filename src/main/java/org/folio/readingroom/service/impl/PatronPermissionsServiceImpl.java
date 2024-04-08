package org.folio.readingroom.service.impl;

import feign.FeignException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.client.feign.UsersClient;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.exception.PatronPermissionsException;
import org.folio.readingroom.repository.PatonPermissionsRepository;
import org.folio.readingroom.service.PatronPermissionsService;
import org.folio.readingroom.service.converter.Mapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class PatronPermissionsServiceImpl implements PatronPermissionsService {
  private final PatonPermissionsRepository patonPermissionsRepository;
  private final Mapper readingRoomMapper;
  private final UsersClient usersClient;

  @Override
  public List<PatronPermission> updatePatronPermissionsByUserId(UUID patronId,
    List<@Valid PatronPermission> patronPermissions) {
    validateUser(patronId, patronPermissions);
    List<PatronPermissionEntity> entities = readingRoomMapper.toEntityList(patronPermissions);
    patonPermissionsRepository.saveAll(entities);
    return patronPermissions;
  }

  private void validateUser(UUID patronId, List<@Valid PatronPermission> patronPermissions) {
    boolean anyInvalid = patronPermissions.stream()
      .anyMatch(permission -> !patronId.equals(permission.getUserId()));
    if (anyInvalid) {
      throw new IllegalArgumentException("patronId does not match with userIds of PatronPermissions");
    }
    try {
      usersClient.getUserById(String.valueOf(patronId));
    } catch (FeignException.NotFound ex) {
      throw new PatronPermissionsException("patronId does not exist in users record");
    } catch (Exception ex) {
      throw new PatronPermissionsException("Error fetching patron: " + ex.getMessage());
    }
  }
}
