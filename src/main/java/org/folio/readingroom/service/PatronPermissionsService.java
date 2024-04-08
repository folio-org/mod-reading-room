package org.folio.readingroom.service;

import jakarta.validation.Valid;
import org.folio.readingroom.domain.dto.PatronPermission;

import java.util.List;
import java.util.UUID;

public interface PatronPermissionsService {
  List<PatronPermission> updatePatronPermissionsByUserId(UUID patronId, List<@Valid PatronPermission> patronPermissions);
}
