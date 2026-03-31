package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.client.ReadingRoomUsersClient;
import org.folio.readingroom.exception.PatronPermissionException;
import org.folio.readingroom.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks
  UserServiceImpl userService;
  @Mock
  ReadingRoomUsersClient readingRoomUsersClient;

  @Test
  void validatePatronPermissionTest() {
    var userId = UUID.randomUUID();
    var patronPermission = new org.folio.readingroom.domain.dto.PatronPermission();
    patronPermission.setUserId(userId);
    var patronPermissions = List.of(patronPermission);
    var randomId = UUID.randomUUID();
    assertThrows(IllegalArgumentException.class, () ->
      userService.validatePatronPermissions(randomId, patronPermissions));

    doNothing().when(readingRoomUsersClient).getReadingRoomUserById(String.valueOf(userId));
    userService.validatePatronPermissions(userId, patronPermissions);
    verify(readingRoomUsersClient).getReadingRoomUserById(String.valueOf(userId));
  }

  @Test
  void validatePatronExistenceTest() {
    var userId = UUID.randomUUID();
    doNothing().when(readingRoomUsersClient).getReadingRoomUserById(String.valueOf(userId));
    userService.validatePatronExistence(userId);
    verify(readingRoomUsersClient).getReadingRoomUserById(String.valueOf(userId));

    doThrow(HttpClientErrorException.NotFound.class).when(readingRoomUsersClient).getReadingRoomUserById(String.valueOf(userId));
    assertThrows(PatronPermissionException.class, () -> userService.validatePatronExistence(userId));

    doThrow(HttpServerErrorException.BadGateway.class).when(readingRoomUsersClient).getReadingRoomUserById(String.valueOf(userId));
    assertThrows(PatronPermissionException.class, () -> userService.validatePatronExistence(userId));
  }
}
