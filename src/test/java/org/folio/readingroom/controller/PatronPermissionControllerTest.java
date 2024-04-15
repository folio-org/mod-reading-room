package org.folio.readingroom.controller;

import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_ID;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID2;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME2;
import static org.folio.readingroom.utils.HelperUtils.createPatronPermission;
import static org.folio.readingroom.utils.HelperUtils.createReadingRoom;
import static org.folio.readingroom.utils.HelperUtils.createServicePoint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class PatronPermissionControllerTest extends BaseIT {

  @Test
  void testSuccessUpdatePatronPermission() throws Exception {
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID, false);
    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
      post("/reading-room")
        .content(asJsonString(readingRoom))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());
    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      READING_ROOM_ID, UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
      put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .content(asJsonString(patronPermission))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful());
  }

  @Test
  void testUserMismatchIdsUpdatePatronPermission() throws Exception {
    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      UUID.randomUUID(), UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
      put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-939eefa8f6df")
        .content(asJsonString(patronPermission))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());
  }

  @Test
  void testUserNotFoundUpdatePatronPermission() throws Exception {
    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      UUID.randomUUID(), UUID.fromString("2305005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
      put("/reading-room-patron-permission/2305005b-ca51-4a04-87fd-939eefa8f6df")
        .content(asJsonString(patronPermission))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());
  }

  @Test
  void testInvalidReadingRoomIdUpdatePatronPermission() throws Exception {
    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      UUID.randomUUID(), UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
        put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .content(asJsonString(patronPermission))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is4xxClientError());
  }

}
