package org.folio.readingroom.service;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
  void validatePatron(UUID patronId, List<PatronPermission> patronPermissions);
}
