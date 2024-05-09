package org.folio.readingroom.service.impl;

import feign.FeignException;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.client.feign.UsersClient;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.exception.PatronPermissionException;
import org.folio.readingroom.service.UserService;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
  private final UsersClient usersClient;

  public void validatePatronPermissions(UUID patronId, List<PatronPermission> patronPermissions) {
    log.info("validatePatron:: Validating patron with id {}", patronId);
    boolean anyInvalid = patronPermissions.stream()
      .anyMatch(permission -> !patronId.equals(permission.getUserId()));
    if (anyInvalid) {
      throw new IllegalArgumentException("patronId does not match with userIds of PatronPermissions");
    }
    validatePatronExistence(patronId);
  }

  public void validatePatronExistence(UUID patronId) {
    try {
      usersClient.getUserById(String.valueOf(patronId));
    } catch (FeignException.NotFound ex) {
      throw new PatronPermissionException("patronId does not exist in users record");
    } catch (Exception ex) {
      throw new PatronPermissionException("Error fetching patron: " + ex.getMessage());
    }
  }
}
