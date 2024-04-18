package org.folio.readingroom.service;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;

public interface UserService {
  void validatePatron(UUID patronId, List<PatronPermission> patronPermissions);

  void validatePatronPermissions(UUID patronId, List<PatronPermission> patronPermissions);

  void validatePatronExistence(UUID patronId);
}
