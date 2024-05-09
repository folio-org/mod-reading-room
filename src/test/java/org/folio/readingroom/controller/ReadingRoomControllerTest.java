package org.folio.readingroom.controller;

import static org.folio.readingroom.utils.HelperUtils.INVALID_SERVICE_POINT_ID;
import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_ID;
import static org.folio.readingroom.utils.HelperUtils.READING_ROOM_NAME;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID2;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_ID3;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME1;
import static org.folio.readingroom.utils.HelperUtils.SERVICE_POINT_NAME2;
import static org.folio.readingroom.utils.HelperUtils.createAccessLog;
import static org.folio.readingroom.utils.HelperUtils.createReadingRoom;
import static org.folio.readingroom.utils.HelperUtils.createServicePoint;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.repository.AccessLogRepository;
import org.folio.readingroom.repository.PatronPermissionRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.spring.service.SystemUserScopedExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ReadingRoomControllerTest extends BaseIT {

  @Autowired
  private ReadingRoomRepository readingRoomRepository;
  @Autowired
  private PatronPermissionRepository patronPermissionRepository;
  @Autowired
  private AccessLogRepository accessLogRepository;
  @Autowired
  private SystemUserScopedExecutionService systemUserScopedExecutionService;

  @Test
  void testCreateReadingRoom() throws Exception {
    removeReadingRoomIfExists();
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
        .andExpect(status().isCreated())
      .andExpect(jsonPath("$.id").value(READING_ROOM_ID.toString()))
      .andExpect(jsonPath("$.name").value(READING_ROOM_NAME))
      .andExpect(jsonPath("$.isPublic").value(false))
      .andExpect(jsonPath("$.servicePoints").isArray())
      .andExpect(jsonPath("$.servicePoints", hasSize(2)))
      .andExpect(jsonPath("$.servicePoints[*].value",
        containsInAnyOrder(SERVICE_POINT_ID1.toString(), SERVICE_POINT_ID2.toString())))
      .andExpect(jsonPath("$.servicePoints[*].label",
        containsInAnyOrder(SERVICE_POINT_NAME1, SERVICE_POINT_NAME2)));
  }

  @Test
  void testCreateReadingRoomWithInvalidScenarios() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID, true);
    readingRoom.servicePoints(Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1)));

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
    readingRoom.servicePoints(Set.of(createServicePoint(INVALID_SERVICE_POINT_ID, "test")));

    // creating Reading room with new id but with invalid servicePoint id
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "ServicePointId " + INVALID_SERVICE_POINT_ID + " doesn't exists in inventory")));

    readingRoom = createReadingRoom(UUID.randomUUID(), true);
    readingRoom.servicePoints(Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2)));

    // creating Reading room with new id but with servicePoint id already associated to another reading room
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "ServicePointId " + SERVICE_POINT_ID1 + " already associated with another Reading room")));

    readingRoom = createReadingRoom(UUID.randomUUID(), true);
    readingRoom.setName("");
    readingRoom.servicePoints(Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2)));

    // creating Reading room with empty name
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "name size must be between 1 and 2147483647")));

    readingRoom = createReadingRoom(UUID.randomUUID(), true);
    var servicePoint = createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1);
    servicePoint.setLabel("");
    readingRoom.setServicePoints(Set.of(servicePoint));

    // creating Reading room with empty service point name
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "label size must be between 1 and 2147483647")));


    // creating Reading room without servicePoints
    readingRoom = createReadingRoom(UUID.randomUUID(), true);
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "servicePoints size must be between 1 and 2147483647")));

    // creating Reading room with duplicate reading room name
    readingRoom = createReadingRoom(UUID.randomUUID(), true);
    readingRoom.servicePoints(Set.of(createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2)));
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(201));

    // creating Reading room without reading room object
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(""))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(400));

  }

  @Test
  void testUpdateReadingRoom() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID, true);
    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom.setServicePoints(servicePoints);

    // creating reading room with 2 service points
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    readingRoom = createReadingRoom(READING_ROOM_ID, false);
    servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID1, "test"));
    readingRoom.servicePoints(servicePoints);
    // updating reading room by updating service point's name and deleting 1 service point
    this.mockMvc.perform(
        put("/reading-room/" + READING_ROOM_ID)
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(READING_ROOM_ID.toString()))
      .andExpect(jsonPath("$.name").value(READING_ROOM_NAME))
      .andExpect(jsonPath("$.isPublic").value(false))
      .andExpect(jsonPath("$.servicePoints").isArray())
      .andExpect(jsonPath("$.servicePoints", hasSize(1)))
      .andExpect(jsonPath("$.servicePoints[0].value").value(SERVICE_POINT_ID1.toString()))
      .andExpect(jsonPath("$.servicePoints[0].label").value("test"));

    // updating reading room by changing name and ispublic and also adding new servicePoint
    readingRoom = createReadingRoom(READING_ROOM_ID, true);
    readingRoom.setName("test");
    servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom.servicePoints(servicePoints);

    this.mockMvc.perform(
        put("/reading-room/" + READING_ROOM_ID)
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(READING_ROOM_ID.toString()))
      .andExpect(jsonPath("$.name").value("test"))
      .andExpect(jsonPath("$.isPublic").value(true))
      .andExpect(jsonPath("$.servicePoints").isArray())
      .andExpect(jsonPath("$.servicePoints", hasSize(2)))
      .andExpect(jsonPath("$.servicePoints[*].value",
        containsInAnyOrder(SERVICE_POINT_ID1.toString(), SERVICE_POINT_ID2.toString())))
      .andExpect(jsonPath("$.servicePoints[*].label",
        containsInAnyOrder(SERVICE_POINT_NAME1, SERVICE_POINT_NAME2)));
  }

  @Test
  void testUpdateReadingRoomInvalidScenarios() throws Exception {
    removeReadingRoomIfExists();
    var readingRoom1 = createReadingRoom(READING_ROOM_ID, true);
    var servicePoints1 = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoom1.servicePoints(servicePoints1);

    // Try to update reading room which is not exists
    this.mockMvc.perform(
        put("/reading-room/" + READING_ROOM_ID)
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(404))
      .andExpect(content().string(containsString(
        "Reading room with id " + READING_ROOM_ID + " doesn't exists")));

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    var readingRoom2 = createReadingRoom(UUID.randomUUID(), true);
    readingRoom2.name("reading_room2");
    var servicePoints2 = Set.of(createServicePoint(SERVICE_POINT_ID3, "test"));
    readingRoom2.servicePoints(servicePoints2);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom2))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    // Try to update reading room 1 with servicePointId which is associated with reading room 2
    readingRoom1 = createReadingRoom(READING_ROOM_ID, false);
    readingRoom1.servicePoints(servicePoints2);

    this.mockMvc.perform(
        put("/reading-room/" + READING_ROOM_ID)
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "ServicePointId " + SERVICE_POINT_ID3 + " already associated with another Reading room")));

    // Try to update reading room 1 with invalid servicePointId
    readingRoom1 = createReadingRoom(READING_ROOM_ID, false);
    readingRoom1.servicePoints(Set.of(createServicePoint(INVALID_SERVICE_POINT_ID, "test")));

    this.mockMvc.perform(
        put("/reading-room/" + READING_ROOM_ID)
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "ServicePointId " + INVALID_SERVICE_POINT_ID + " doesn't exists in inventory")));

    // Try to update reading room with invalid id in path param
    this.mockMvc.perform(
        put("/reading-room/" + UUID.randomUUID())
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422))
      .andExpect(content().string(containsString(
        "The ID provided in the request URL does not match the ID of the resource in the request body")));

  }

  @Test
  void testGetReadingRoom() throws Exception {
    removeReadingRoomIfExists();

    //No reading room is created so there will be 0 record in get call
    this.mockMvc.perform(
        get("/reading-room")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(0))
      .andExpect(jsonPath("readingRooms").isArray())
      .andExpect(jsonPath("readingRooms", hasSize(0)));

    var readingRoom1 = createReadingRoom(READING_ROOM_ID, false);
    var servicePoints1 = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoom1.servicePoints(servicePoints1);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    //1 reading room is created so there will be 1 record in get call
    this.mockMvc.perform(
        get("/reading-room")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(1))
      .andExpect(jsonPath("readingRooms").isArray())
      .andExpect(jsonPath("readingRooms", hasSize(1)))
      .andExpect(jsonPath("$.readingRooms[0].id",
        containsString(READING_ROOM_ID.toString())))
      .andExpect(jsonPath("$.readingRooms[0].name",
        containsString(READING_ROOM_NAME)))
      .andExpect(jsonPath("$.readingRooms[0].isPublic")
        .value(false))
      .andExpect(jsonPath("$.readingRooms[0].servicePoints[0].value",
        containsString(SERVICE_POINT_ID1.toString())))
      .andExpect(jsonPath("$.readingRooms[0].servicePoints[0].label",
        containsString(SERVICE_POINT_NAME1)));


    var readingRoomId = UUID.randomUUID();
    var readingRoom2 = createReadingRoom(readingRoomId, true);
    readingRoom2.setName("test");
    var servicePoints2 = Set.of(createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom2.servicePoints(servicePoints2);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom2))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    //2 reading rooms are created so there will be 2 records in get call
    this.mockMvc.perform(
        get("/reading-room")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(2))
      .andExpect(jsonPath("readingRooms").isArray())
      .andExpect(jsonPath("readingRooms", hasSize(2)))
      .andExpect(jsonPath("$.readingRooms[*].id",
        containsInAnyOrder(READING_ROOM_ID.toString(), readingRoomId.toString())))
      .andExpect(jsonPath("$.readingRooms[*].name",
        containsInAnyOrder(READING_ROOM_NAME, "test")))
      .andExpect(jsonPath("$.readingRooms[*].isPublic",
        containsInAnyOrder(true, false)))
      .andExpect(jsonPath("$.readingRooms[*].servicePoints[*].value",
        containsInAnyOrder(SERVICE_POINT_ID1.toString(), SERVICE_POINT_ID2.toString())))
      .andExpect(jsonPath("$.readingRooms[*].servicePoints[*].label",
        containsInAnyOrder(SERVICE_POINT_NAME1, SERVICE_POINT_NAME2)));
  }

  @Test
  void testGetReadingRoomNotIncludingDeleted() throws Exception {
    removeReadingRoomIfExists();
    ReadingRoom readingRoom = createReadingRoom(READING_ROOM_ID, false);
    var servicePoints = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1),
      createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom.servicePoints(servicePoints);
    //creating 1 reading room record
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());
    //soft deleting that record
    this.mockMvc.perform(delete("/reading-room/" + READING_ROOM_ID)
        .headers(defaultHeaders()))
      .andExpect(status().isNoContent());
    //since the only record present is deleted when we retrieve we get 0 records;
    this.mockMvc.perform(
        get("/reading-room")
          .param("includeDeleted", "false")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(0))
      .andExpect(jsonPath("readingRooms").isArray())
      .andExpect(jsonPath("readingRooms", hasSize(0)));

    removeReadingRoomIfExists();

    //creating 2 reading room records
    ReadingRoom readingRoom2 = createReadingRoom(READING_ROOM_ID, false);
    var servicePointsForReadingRoom2 = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoom2.servicePoints(servicePointsForReadingRoom2);

    var readingRoomIdForReadingRoom3 = UUID.randomUUID();
    var readingRoom3 = createReadingRoom(readingRoomIdForReadingRoom3, true);
    readingRoom3.setName("test");
    var servicePoints3 = Set.of(createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom3.servicePoints(servicePoints3);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom2))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom3))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    //soft deleting  1 record
    this.mockMvc.perform(delete("/reading-room/" + readingRoomIdForReadingRoom3)
        .headers(defaultHeaders()))
      .andExpect(status().isNoContent());

    //as one record is soft deleted we  retrieve only 1 non deleted record
    this.mockMvc.perform(
        get("/reading-room")
          .param("includeDeleted", "false")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(1))
      .andExpect(jsonPath("readingRooms").isArray())
      .andExpect(jsonPath("readingRooms", hasSize(1)))
      .andExpect(jsonPath("$.readingRooms[0].id",
        containsString(READING_ROOM_ID.toString())))
      .andExpect(jsonPath("$.readingRooms[0].name",
        containsString(READING_ROOM_NAME)))
      .andExpect(jsonPath("$.readingRooms[0].isPublic")
        .value(false))
      .andExpect(jsonPath("$.readingRooms[0].servicePoints[0].value",
        containsString(SERVICE_POINT_ID1.toString())))
      .andExpect(jsonPath("$.readingRooms[0].servicePoints[0].label",
        containsString(SERVICE_POINT_NAME1)));
  }

  @Test
  void testGetReadingRoomIncludingDeleted() throws Exception {
    removeReadingRoomIfExists();
    //creating 2 reading room records
    ReadingRoom readingRoom1 = createReadingRoom(READING_ROOM_ID, false);
    var servicePoints1 = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoom1.servicePoints(servicePoints1);

    var readingRoomIdForReadingRoom2 = UUID.randomUUID();
    var readingRoom2 = createReadingRoom(readingRoomIdForReadingRoom2, true);
    readingRoom2.setName("test");
    var servicePoints2 = Set.of(createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom2.servicePoints(servicePoints2);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom2))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    //soft deleting  1 record
    this.mockMvc.perform(delete("/reading-room/" + readingRoomIdForReadingRoom2)
        .headers(defaultHeaders()))
      .andExpect(status().isNoContent());

    //as includingDeleted flag is true we get all deleted and non deleted records
    this.mockMvc.perform(
        get("/reading-room")
          .headers(defaultHeaders())
          .param("includeDeleted", "true")
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(2))
      .andExpect(jsonPath("readingRooms").isArray())
      .andExpect(jsonPath("readingRooms", hasSize(2)))
      .andExpect(jsonPath("$.readingRooms[*].id",
        containsInAnyOrder(READING_ROOM_ID.toString(), readingRoomIdForReadingRoom2.toString())))
      .andExpect(jsonPath("$.readingRooms[*].name",
        containsInAnyOrder(READING_ROOM_NAME, "test")))
      .andExpect(jsonPath("$.readingRooms[*].isPublic",
        containsInAnyOrder(true, false)))
      .andExpect(jsonPath("$.readingRooms[*].servicePoints[*].value",
        containsInAnyOrder(SERVICE_POINT_ID1.toString())))
      .andExpect(jsonPath("$.readingRooms[*].servicePoints[*].label",
        containsInAnyOrder(SERVICE_POINT_NAME1)));

  }

  @Test
  void testDeleteReadingRoom() throws Exception {
    removeReadingRoomIfExists();
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
    this.mockMvc.perform(delete("/reading-room/" + READING_ROOM_ID)
        .headers(defaultHeaders()))
      .andExpect(status().isNoContent());

    // Trying to create one more reading room with same servicePoints which was associated with deleted RR
    readingRoom = createReadingRoom(UUID.randomUUID(), false);
    readingRoom.servicePoints(servicePoints);
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());
  }

  @Test
  void testDeleteReadingRoomWithInvalidScenario() throws Exception {
    removeReadingRoomIfExists();
    this.mockMvc.perform(delete("/reading-room/" + READING_ROOM_ID)
        .headers(defaultHeaders()))
      .andExpect(status().is(404))
      .andExpect(content().string(containsString(
        "Reading room with id " + READING_ROOM_ID + " doesn't exists")));

  }

  @Test
  void testCreateAccessLog() throws Exception {
    removeReadingRoomIfExists();
    var accessLog = createAccessLog(UUID.randomUUID(), AccessLog.ActionEnum.ALLOWED);
    this.mockMvc.perform(post("/reading-room/" + UUID.randomUUID() + "/access-log")
        .content(asJsonString(accessLog))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(422));

    var readingRoom1 = createReadingRoom(READING_ROOM_ID, false);
    var servicePoints1 = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoom1.servicePoints(servicePoints1);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    this.mockMvc.perform(post("/reading-room/" + READING_ROOM_ID + "/access-log")
        .content(asJsonString(accessLog))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(201));

    this.mockMvc.perform(post("/reading-room/" + READING_ROOM_ID + "/access-log")
        .content(asJsonString(accessLog))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(409));
  }

  private void removeReadingRoomIfExists() {
    systemUserScopedExecutionService.executeAsyncSystemUserScoped(TENANT, () -> {
      patronPermissionRepository.deleteAll();
      accessLogRepository.deleteAll();
      readingRoomRepository.deleteAll();
    });
  }

}
