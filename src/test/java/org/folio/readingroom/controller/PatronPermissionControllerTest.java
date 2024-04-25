package org.folio.readingroom.controller;

import static org.folio.readingroom.domain.dto.AccessLog.ActionEnum.ALLOWED;
import static org.folio.readingroom.domain.dto.PatronPermission.AccessEnum.NOT_ALLOWED;
import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_ID_FOR_PATRON_TEST;
import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_ID_FOR_PATRON_TEST_1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID4;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME3;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME4;
import static org.folio.readingroom.utils.HelperUtils.createPatronPermission;
import static org.folio.readingroom.utils.HelperUtils.createReadingRoom;
import static org.folio.readingroom.utils.HelperUtils.createServicePoint;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.spring.service.SystemUserScopedExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class PatronPermissionControllerTest extends BaseIT {
  @Autowired
  private ReadingRoomRepository readingRoomRepository;
  @Autowired
  private SystemUserScopedExecutionService systemUserScopedExecutionService;

  @Test
  void testSuccessUpdatePatronPermission() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID_FOR_PATRON_TEST, false);
    readingRoom.setName("TestPatron");
    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID4, SERVICE_POINT_NAME3));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
      post("/reading-room")
        .content(asJsonString(readingRoom))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());
    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      READING_ROOM_ID_FOR_PATRON_TEST, UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
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

  @Test
  void testSuccessGetPatronPermission() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID_FOR_PATRON_TEST, false);
    readingRoom.setName("TestPatronRR");
    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID4, SERVICE_POINT_NAME3));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    this.mockMvc.perform(
        get("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$[0].readingRoomId").value("7a5abc9f-f3d7-4856-b8d7-6712462ca009"))
      .andExpect(jsonPath("$.[0].userId").value("2205005b-ca51-4a04-87fd-938eefa8f6df"))
      .andExpect(jsonPath("$.[0].access").value(NOT_ALLOWED.getValue()))
      .andExpect(jsonPath("$", hasSize(1)));

    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      READING_ROOM_ID_FOR_PATRON_TEST, UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
        put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .content(asJsonString(patronPermission))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful());

    this.mockMvc.perform(
        get("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$[0].readingRoomId").value("7a5abc9f-f3d7-4856-b8d7-6712462ca009"))
      .andExpect(jsonPath("$.[0].userId").value("2205005b-ca51-4a04-87fd-938eefa8f6df"))
      .andExpect(jsonPath("$.[0].access").value(ALLOWED.getValue()))
      .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testGetPatronPermissionForDeleted() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID_FOR_PATRON_TEST, false);
    readingRoom.setName("TestPatronRR");
    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID4, SERVICE_POINT_NAME3));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    ReadingRoom readingRoom2 = createReadingRoom(READING_ROOM_ID_FOR_PATRON_TEST_1, false);
    readingRoom2.setName("TestPatronRR1");
    var servicePoints2 = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME4));
    readingRoom2.servicePoints(servicePoints2);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom2))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    this.mockMvc.perform(
        get("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$[0].readingRoomId").value("8a5abc9f-f3d7-4856-b8d7-6712462ca009"))
      .andExpect(jsonPath("$.[0].userId").value("2205005b-ca51-4a04-87fd-938eefa8f6df"))
      .andExpect(jsonPath("$.[0].access").value(NOT_ALLOWED.getValue()))
      .andExpect(jsonPath("$", hasSize(2)));

    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      READING_ROOM_ID_FOR_PATRON_TEST, UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
        put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .content(asJsonString(patronPermission))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful());

    PatronPermission patronPermission2 = createPatronPermission(UUID.randomUUID(),
      READING_ROOM_ID_FOR_PATRON_TEST_1, UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
        put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .content(asJsonString(patronPermission2))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful());

    this.mockMvc.perform(delete("/reading-room/" + READING_ROOM_ID_FOR_PATRON_TEST_1)
        .headers(defaultHeaders()))
      .andExpect(status().isNoContent());

    this.mockMvc.perform(
        get("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$[0].readingRoomId").value("7a5abc9f-f3d7-4856-b8d7-6712462ca009"))
      .andExpect(jsonPath("$.[0].userId").value("2205005b-ca51-4a04-87fd-938eefa8f6df"))
      .andExpect(jsonPath("$.[0].access").value(ALLOWED.getValue()))
      .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testSuccessGetPatronPermissionWithServicePoint() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID_FOR_PATRON_TEST, false);
    readingRoom.setName("TestPatronRR");
    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID4, SERVICE_POINT_NAME3));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
      post("/reading-room")
        .content(asJsonString(readingRoom))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      READING_ROOM_ID_FOR_PATRON_TEST, UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    this.mockMvc.perform(
      put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df?")
        .content(asJsonString(patronPermission))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful());

    this.mockMvc.perform(
      get("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df"
        + "?servicePointId=9865c56f-608d-4c93-94fe-f365f16e6971")
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$[0].readingRoomId").value("7a5abc9f-f3d7-4856-b8d7-6712462ca009"))
      .andExpect(jsonPath("$.[0].userId").value("2205005b-ca51-4a04-87fd-938eefa8f6df"))
      .andExpect(jsonPath("$.[0].access").value(ALLOWED.getValue()))
      .andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testSuccessGetPatronPermissionWithNonAssociatedServicePoint() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID_FOR_PATRON_TEST, false);
    readingRoom.setName("TestPatronRR");

    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID4, SERVICE_POINT_NAME3));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
      post("/reading-room")
        .content(asJsonString(readingRoom))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    this.mockMvc.perform(
      get("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df"
        + "?servicePointId=7c5abc9f-f3d7-4856-b8d7-6712462ca008")
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is2xxSuccessful())
      .andExpect(jsonPath("$", hasSize(1)));
  }

  private void removeReadingRoomIfExists() {
    systemUserScopedExecutionService.executeAsyncSystemUserScoped(TENANT, () -> {
      if (readingRoomRepository.existsById(READING_ROOM_ID_FOR_PATRON_TEST)) {
        readingRoomRepository.deleteById(READING_ROOM_ID_FOR_PATRON_TEST);
      }
    });
  }
}
