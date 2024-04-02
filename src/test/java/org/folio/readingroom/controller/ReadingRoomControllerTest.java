package org.folio.readingroom.controller;

import static org.folio.readingroom.utils.HelperUtils.INVALID_SERVICE_POINT_ID;
import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_ID;
import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_NAME;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID2;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME2;
import static org.folio.readingroom.utils.HelperUtils.createReadingRoom;
import static org.folio.readingroom.utils.HelperUtils.createServicePoint;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.spring.service.SystemUserScopedExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ReadingRoomControllerTest extends BaseIT {

  @Autowired
  private ReadingRoomRepository readingRoomRepository;
  @Autowired
  private SystemUserScopedExecutionService systemUserScopedExecutionService;

  @Test
  void testCreateReadingRoom() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID, false);
    var servicePoints = List.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(READING_ROOM_ID.toString()))
      .andExpect(jsonPath("$.name").value(READING_ROOM_NAME))
      .andExpect(jsonPath("$.ispublic").value(false))
      .andExpect(jsonPath("$.servicePoints").isArray())
      .andExpect(jsonPath("$.servicePoints", hasSize(2)))
      .andExpect(jsonPath("$.servicePoints[*].id",
        containsInAnyOrder(SERVICE_POINT_ID1.toString(), SERVICE_POINT_ID2.toString())))
      .andExpect(jsonPath("$.servicePoints[*].name",
        containsInAnyOrder(SERVICE_POINT_NAME1, SERVICE_POINT_NAME2)));
  }

  @Test
  void testCreateReadingRoomWithInvalidScenarios() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID, true);
    readingRoom.servicePoints(List.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1)));

    // creating Reading room
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    // creating Reading room with existing id
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(409))
      .andExpect(content().string(containsString("Reading room with id " + READING_ROOM_ID + " already exists")));

    readingRoom = createReadingRoom(UUID.randomUUID(), true);
    readingRoom.servicePoints(List.of(createServicePoint(INVALID_SERVICE_POINT_ID, "test")));

    // creating Reading room with new id but with invalid servicePoint id
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(404))
      .andExpect(content().string(containsString("One of the ServicePointId doesn't exist")));

    readingRoom = createReadingRoom(UUID.randomUUID(), true);
    readingRoom.servicePoints(List.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2)));

    // creating Reading room with new id but with servicePoint id already associated to another reading room
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(409))
      .andExpect(content().string(containsString(
        "One of the servicePointId already associated with existing Reading room")));
  }

  private void removeReadingRoomIfExists() {
    systemUserScopedExecutionService.executeAsyncSystemUserScoped(TENANT, () -> {
      if (readingRoomRepository.existsById(READING_ROOM_ID)) {
        readingRoomRepository.deleteById(READING_ROOM_ID);
      }
    });
  }

}
