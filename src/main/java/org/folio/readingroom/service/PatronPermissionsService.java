package org.folio.readingroom.service;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;

public interface PatronPermissionsService {
  List<PatronPermission> updatePatronPermissionsByUserId(UUID patronId,
    List<@Valid PatronPermission> patronPermissions);
}
