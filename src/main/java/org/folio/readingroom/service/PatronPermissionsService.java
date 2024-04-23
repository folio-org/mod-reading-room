package org.folio.readingroom.service;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;

public interface PatronPermissionsService {
  List<PatronPermission> updatePatronPermissionsByUserId(UUID patronId,
    List<PatronPermission> patronPermissions);

  List<PatronPermission> getPatronPermissionsByUserId(UUID patronId, UUID servicePointId);
}
