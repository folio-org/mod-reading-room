package org.folio.readingroom.client;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "users", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface ReadingRoomUsersClient {

  @GetExchange(value = "/{userId}")
  void getReadingRoomUserById(@PathVariable String userId);
}
