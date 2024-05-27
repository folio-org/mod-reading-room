package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import feign.FeignException;
import java.util.List;
import java.util.UUID;
import org.folio.readingroom.client.feign.UsersClient;
import org.folio.readingroom.exception.PatronPermissionException;
import org.folio.readingroom.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  UserServiceImpl userService;
  @Mock
  UsersClient usersClient;

  @Test
  void validatePatronPermissionTest() {
    var userId = UUID.randomUUID();
    var patronPermission = new org.folio.readingroom.domain.dto.PatronPermission();
    patronPermission.setUserId(userId);
    var patronPermissions = List.of(patronPermission);
    assertThrows(IllegalArgumentException.class, () ->
      userService.validatePatronPermissions(UUID.randomUUID(), patronPermissions));

    doNothing().when(usersClient).getUserById(String.valueOf(userId));
    userService.validatePatronPermissions(userId, patronPermissions);
    verify(usersClient).getUserById(String.valueOf(userId));
  }

  @Test
  void validatePatronExistenceTest() {
    var userId = UUID.randomUUID();
    doNothing().when(usersClient).getUserById(String.valueOf(userId));
    userService.validatePatronExistence(userId);
    verify(usersClient).getUserById(String.valueOf(userId));

    doThrow(FeignException.NotFound.class).when(usersClient).getUserById(String.valueOf(userId));
    assertThrows(PatronPermissionException.class, () -> userService.validatePatronExistence(userId));

    doThrow(FeignException.BadGateway.class).when(usersClient).getUserById(String.valueOf(userId));
    assertThrows(PatronPermissionException.class, () -> userService.validatePatronExistence(userId));
  }
}
