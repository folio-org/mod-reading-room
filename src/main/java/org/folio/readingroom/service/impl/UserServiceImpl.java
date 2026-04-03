package org.folio.readingroom.service.impl;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.client.ReadingRoomUsersClient;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.exception.PatronPermissionException;
import org.folio.readingroom.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@AllArgsConstructor
@Log4j2
public class UserServiceImpl implements UserService {
  private final ReadingRoomUsersClient readingRoomUsersClient;

  public void validatePatronPermissions(UUID patronId, List<PatronPermission> patronPermissions) {
    log.info("validatePatron:: validating patron permissions");
    boolean anyInvalid = patronPermissions.stream()
      .anyMatch(permission -> !patronId.equals(permission.getUserId()));
    if (anyInvalid) {
      throw new IllegalArgumentException("patronId does not match with userIds of PatronPermissions");
    }
    validatePatronExistence(patronId);
  }

  public void validatePatronExistence(UUID patronId) {
    try {
      readingRoomUsersClient.getReadingRoomUserById(String.valueOf(patronId));
    } catch (HttpClientErrorException.NotFound ex) {
      throw new PatronPermissionException("patronId does not exist in users record");
    } catch (Exception ex) {
      throw new PatronPermissionException("Error fetching patron: " + ex.getMessage());
    }
  }
}
