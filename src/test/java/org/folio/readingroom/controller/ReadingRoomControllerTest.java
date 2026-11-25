package org.folio.readingroom.controller;

import static org.folio.readingroom.domain.dto.AccessLog.ActionEnum.ALLOWED;
import static org.folio.readingroom.domain.dto.AccessLog.ActionEnum.DENIED;
import static org.folio.readingroom.utils.HelperUtils.BAD_GATEWAY_SERVICE_POINT_ID;
import static org.folio.readingroom.utils.HelperUtils.ERROR_SERVICE_POINT_ID;
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
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import javax.sql.DataSource;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.Metadata;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.repository.AccessLogRepository;
import org.folio.readingroom.repository.PatronPermissionRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.spring.integration.XOkapiHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.ResultActions;

class ReadingRoomControllerTest extends BaseIT {

  private static final String UTC_MILLIS_PATTERN = "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z";

  @Autowired
  private ReadingRoomRepository readingRoomRepository;
  @Autowired
  private PatronPermissionRepository patronPermissionRepository;
  @Autowired
  private AccessLogRepository accessLogRepository;
  @Autowired
  private DataSource dataSource;

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

    readingRoom.servicePoints(Set.of(createServicePoint(BAD_GATEWAY_SERVICE_POINT_ID, "test")));

    // creating Reading room with new id but getting bad gateway during service point validation
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(502));

    readingRoom.servicePoints(Set.of(createServicePoint(ERROR_SERVICE_POINT_ID, "test")));

    // creating Reading room with new id but getting exception during service point validation
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(500));

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
    //since the only record present is deleted when we retrieve we get 0 records
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
    var accessLog = createAccessLog(UUID.randomUUID(), ALLOWED);
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

  @Test
  void testCreateAndGetAccessLog() throws Exception {
    removeReadingRoomIfExists();
    var readingRoomId1 = UUID.randomUUID();
    var readingRoomName1 = "readingRoomName1";
    var readingRoom1 = createReadingRoom(readingRoomId1, false);
    readingRoom1.setName(readingRoomName1);
    var servicePoints1 = Set.of(createServicePoint(SERVICE_POINT_ID1, SERVICE_POINT_NAME1));
    readingRoom1.servicePoints(servicePoints1);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom1))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    var readingRoomId2 = UUID.randomUUID();
    var readingRoom2 = createReadingRoom(readingRoomId2, false);
    var readingRoomName2 = "readingRoomName2";
    readingRoom2.setName(readingRoomName2);
    var servicePoints2 = Set.of(createServicePoint(SERVICE_POINT_ID2, SERVICE_POINT_NAME2));
    readingRoom2.servicePoints(servicePoints2);

    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom2))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());

    // access log entry is created for readingRoomId1
    var accessLog1 = createAccessLog(UUID.randomUUID(), readingRoomId1, SERVICE_POINT_ID1, readingRoomName1, ALLOWED);

    this.mockMvc.perform(post("/reading-room/" + readingRoomId1 + "/access-log")
        .content(asJsonString(accessLog1))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(201));

    // access log entry is created for readingRoomId2
    var accessLog2 = createAccessLog(UUID.randomUUID(), readingRoomId2, SERVICE_POINT_ID2, readingRoomName2, DENIED);

    this.mockMvc.perform(post("/reading-room/" + readingRoomId2 + "/access-log")
        .content(asJsonString(accessLog2))
        .headers(defaultHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().is(201));

    // No cql query is passed, so it will fetch all the records
    this.mockMvc.perform(
        get("/reading-room/access-log")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(2))
      .andExpect(jsonPath("accessLogs").isArray())
      .andExpect(jsonPath("accessLogs", hasSize(2)))
      .andExpect(jsonPath("$.accessLogs[*].readingRoomId",
        containsInAnyOrder(readingRoomId1.toString(), readingRoomId2.toString())));

    // passing valid readingRoomId in cql query
    this.mockMvc.perform(
        get("/reading-room/access-log")
          .param("query", "readingRoomId=" + readingRoomId1)
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(1))
      .andExpect(jsonPath("accessLogs").isArray())
      .andExpect(jsonPath("accessLogs", hasSize(1)))
      .andExpect(jsonPath("$.accessLogs[0].readingRoomId",
        containsString(readingRoomId1.toString())));

    // passing valid readingRoomName and servicePointId in cql query
    this.mockMvc.perform(
        get("/reading-room/access-log")
          .param("query", "readingRoomName=" + readingRoomName2 + " and servicePointId=" + SERVICE_POINT_ID2)
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(1))
      .andExpect(jsonPath("accessLogs").isArray())
      .andExpect(jsonPath("accessLogs", hasSize(1)))
      .andExpect(jsonPath("$.accessLogs[0].readingRoomId",
        containsString(readingRoomId2.toString())));

    // passing valid readingRoomName and action in cql query
    this.mockMvc.perform(
        get("/reading-room/access-log")
          .param("query", "action=ALLOWED")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(1))
      .andExpect(jsonPath("accessLogs").isArray())
      .andExpect(jsonPath("accessLogs", hasSize(1)))
      .andExpect(jsonPath("$.accessLogs[0].readingRoomId", is(readingRoomId1.toString())));

    // passing valid readingRoomName and action in cql query
    this.mockMvc.perform(
        get("/reading-room/access-log")
          .param("query", "action=DENIED")
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(1))
      .andExpect(jsonPath("accessLogs").isArray())
      .andExpect(jsonPath("accessLogs", hasSize(1)))
      .andExpect(jsonPath("$.accessLogs[0].readingRoomId", is(readingRoomId2.toString())));

    // Passing invalid readingRoomId in cql query
    this.mockMvc.perform(
        get("/reading-room/access-log")
          .param("query", "readingRoomId=" + UUID.randomUUID() + " and servicePointId=" + SERVICE_POINT_ID2)
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("totalRecords").value(0))
      .andExpect(jsonPath("accessLogs").isArray())
      .andExpect(jsonPath("accessLogs", hasSize(0)));
  }

  @Test
  void testCreateAccessLogReturnsMetadata() throws Exception {
    removeReadingRoomIfExists();
    createReadingRoomWithServicePoint(READING_ROOM_ID, SERVICE_POINT_ID1);

    var body = postAccessLog(createAccessLog(UUID.randomUUID(), ALLOWED), defaultHeaders())
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.metadata.createdDate", matchesPattern(UTC_MILLIS_PATTERN)))
      .andExpect(jsonPath("$.metadata.createdByUserId").value(okapiUserId()))
      .andReturn().getResponse().getContentAsString();

    String createdDate = JsonPath.read(body, "$.metadata.createdDate");
    assertEquals(createdDate, JsonPath.read(body, "$.metadata.updatedDate"));
    assertEquals(okapiUserId(), JsonPath.read(body, "$.metadata.updatedByUserId"));
    var age = Duration.between(Instant.parse(createdDate), Instant.now()).abs();
    assertTrue(age.compareTo(Duration.ofMinutes(1)) < 0);
  }

  @Test
  void testCreateAccessLogIgnoresClientSuppliedMetadata() throws Exception {
    removeReadingRoomIfExists();
    createReadingRoomWithServicePoint(READING_ROOM_ID, SERVICE_POINT_ID1);
    var bogusUser = UUID.randomUUID();
    var accessLog = createAccessLog(UUID.randomUUID(), ALLOWED)
      .metadata(new Metadata().createdDate("2000-01-01T00:00:00.000Z").createdByUserId(bogusUser)
        .updatedDate("2000-01-02T00:00:00.000Z").updatedByUserId(bogusUser));

    postAccessLog(accessLog, defaultHeaders())
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.metadata.createdDate", not("2000-01-01T00:00:00.000Z")))
      .andExpect(jsonPath("$.metadata.updatedDate", not("2000-01-02T00:00:00.000Z")))
      .andExpect(jsonPath("$.metadata.createdByUserId").value(okapiUserId()))
      .andExpect(jsonPath("$.metadata.updatedByUserId").value(okapiUserId()));

    this.mockMvc.perform(
        get("/reading-room/access-log")
          .param("query", "readingRoomId=" + READING_ROOM_ID)
          .headers(defaultHeaders())
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.accessLogs[0].metadata.createdByUserId").value(okapiUserId()))
      .andExpect(jsonPath("$.accessLogs[0].metadata.createdDate", not("2000-01-01T00:00:00.000Z")))
      .andExpect(jsonPath("$.accessLogs[0].metadata.updatedDate", not("2000-01-02T00:00:00.000Z")));
  }

  @Test
  void testCreateAccessLogAcceptsEmptyAndPartialClientMetadata() throws Exception {
    removeReadingRoomIfExists();
    createReadingRoomWithServicePoint(READING_ROOM_ID, SERVICE_POINT_ID1);
    var withEmptyMetadata = createAccessLog(UUID.randomUUID(), ALLOWED).metadata(new Metadata());
    var withPartialMetadata = createAccessLog(UUID.randomUUID(), DENIED)
      .metadata(new Metadata().createdByUserId(UUID.randomUUID()));

    for (var accessLog : List.of(withEmptyMetadata, withPartialMetadata)) {
      postAccessLog(accessLog, defaultHeaders())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.metadata.createdDate", matchesPattern(UTC_MILLIS_PATTERN)))
        .andExpect(jsonPath("$.metadata.createdByUserId").value(okapiUserId()));
    }
  }

  @Test
  void testCreateAccessLogWithoutOkapiUserIdOmitsUserIds() throws Exception {
    removeReadingRoomIfExists();
    createReadingRoomWithServicePoint(READING_ROOM_ID, SERVICE_POINT_ID1);
    var headers = defaultHeaders();
    headers.remove(XOkapiHeaders.USER_ID);

    postAccessLog(createAccessLog(UUID.randomUUID(), ALLOWED), headers)
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.metadata.createdDate", matchesPattern(UTC_MILLIS_PATTERN)))
      .andExpect(jsonPath("$.metadata.updatedDate", matchesPattern(UTC_MILLIS_PATTERN)))
      .andExpect(jsonPath("$.metadata.createdByUserId").doesNotExist())
      .andExpect(jsonPath("$.metadata.updatedByUserId").doesNotExist());
  }

  @Test
  void testGetAccessLogsReturnMetadataForEveryEntry() throws Exception {
    removeReadingRoomIfExists();
    createReadingRoomWithServicePoint(READING_ROOM_ID, SERVICE_POINT_ID1);
    postAccessLog(createAccessLog(UUID.randomUUID(), ALLOWED), defaultHeaders()).andExpect(status().isCreated());
    postAccessLog(createAccessLog(UUID.randomUUID(), DENIED), defaultHeaders()).andExpect(status().isCreated());

    var body = this.mockMvc.perform(
        get("/reading-room/access-log")
          .headers(defaultHeaders())
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.totalRecords").value(2))
      .andExpect(jsonPath("$.accessLogs[*].metadata.createdDate", hasSize(2)))
      .andExpect(jsonPath("$.accessLogs[*].metadata.createdDate", everyItem(matchesPattern(UTC_MILLIS_PATTERN))))
      .andReturn().getResponse().getContentAsString();

    List<String> created = JsonPath.read(body, "$.accessLogs[*].metadata.createdDate");
    List<String> updated = JsonPath.read(body, "$.accessLogs[*].metadata.updatedDate");
    assertEquals(created, updated);
  }

  @Test
  void testGetAccessLogLegacyEntryWithoutNameServicePointAndUpdatedFields() throws Exception {
    removeReadingRoomIfExists();
    createReadingRoomWithServicePoint(READING_ROOM_ID, SERVICE_POINT_ID1);
    var legacyId = UUID.randomUUID();
    insertLegacyAccessLog(legacyId);
    var originalZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    try {
      this.mockMvc.perform(
          get("/reading-room/access-log")
            .param("query", "readingRoomId=" + READING_ROOM_ID)
            .headers(defaultHeaders())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalRecords").value(1))
        .andExpect(jsonPath("$.accessLogs[0].id").value(legacyId.toString()))
        .andExpect(jsonPath("$.accessLogs[0].readingRoomName").doesNotExist())
        .andExpect(jsonPath("$.accessLogs[0].servicePointId").doesNotExist())
        .andExpect(jsonPath("$.accessLogs[0].metadata.createdDate").value("2025-01-15T10:20:30.123Z"))
        .andExpect(jsonPath("$.accessLogs[0].metadata.updatedDate").value("2025-01-15T10:20:30.123Z"))
        .andExpect(jsonPath("$.accessLogs[0].metadata.createdByUserId").doesNotExist())
        .andExpect(jsonPath("$.accessLogs[0].metadata.updatedByUserId").doesNotExist());
    } finally {
      TimeZone.setDefault(originalZone);
    }
  }

  @Test
  void testGetAccessLogsByCreatedOrUpdatedDateRange_returnsEntriesOfThoseUtcDays() throws Exception {
    insertEntriesAroundJanuary2026();

    assertEquals(ids(3, 2), getAccessLogIds("createdDate>=2026-01-01 and createdDate<2026-02-01"));
    assertEquals(ids(3, 2), getAccessLogIds("updatedDate>=2026-01-01 and updatedDate<2026-02-01"));
    assertEquals(ids(3, 2), getAccessLogIds("createdDate<=2026-01-31 and updatedDate>2025-12-31"));
    // the date range the FOLIO UI builds
    assertEquals(ids(3, 2),
      getAccessLogIds("createdDate>=\"2026-01-01T00:00:00.000\" and createdDate<=\"2026-01-31T23:59:59.999\""));
  }

  @Test
  void testGetAccessLogsByDateAndExistingFilter_returnsEntriesMatchingBoth() throws Exception {
    insertEntriesAroundJanuary2026();

    assertEquals(ids(2), getAccessLogIds("patronId==" + uuid(101) + " and createdDate=2026-01-01"));
    assertEquals(ids(1), getAccessLogIds("userId==" + uuid(201) + " and createdDate<2026-01-01"));
    assertEquals(ids(3), getAccessLogIds("action==DENIED and updatedDate>=2026-01-01"));
    assertEquals(ids(4, 3, 2), getAccessLogIds("readingRoomId==" + READING_ROOM_ID
      + " and servicePointId==" + SERVICE_POINT_ID1 + " and createdDate>=2026-01-01"));
  }

  @Test
  void testGetAccessLogsSortedByDate_returnsEntriesInRequestedOrder() throws Exception {
    insertEntriesAroundJanuary2026();

    assertEquals(ids(4, 3, 2, 1), getAccessLogIds(null));
    assertEquals(ids(1, 2, 3, 4), getAccessLogIds("cql.allRecords=1 sortby createdDate/sort.ascending"));
    assertEquals(ids(4, 3, 2, 1), getAccessLogIds("cql.allRecords=1 sortby updatedDate/sort.descending"));
    assertEquals(ids(2, 3, 4), getAccessLogIds("createdDate>=2026-01-01 sortby updatedDate/sort.ascending"));
  }

  @Test
  void testGetAccessLogsByUtcOffsetValues_returnsEntriesOfThatInstantRange() throws Exception {
    insertEntriesAroundJanuary2026();

    assertEquals(ids(2),
      getAccessLogIds("createdDate>=2026-01-01T00:00:00Z and createdDate<2026-01-31T12:00:00+02:00"));
    assertEquals(ids(4, 3), getAccessLogIds("createdDate>2025-12-31T19:00:00-05:00"));
  }

  @Test
  void testGetAccessLogsPagedThroughSortedResults_returnsEveryEntryOnce() throws Exception {
    prepareReadingRoom();
    // 11 and 12 share their creation time, and 11 is stored first
    insertAccessLog(uuid(11), uuid(201), uuid(101), "ALLOWED", "2026-03-01T10:00:00Z");
    insertAccessLog(uuid(12), uuid(201), uuid(101), "ALLOWED", "2026-03-01T10:00:00Z");
    insertAccessLog(uuid(13), uuid(201), uuid(101), "DENIED", "2026-03-01T09:00:00Z");
    insertAccessLog(uuid(14), uuid(201), uuid(101), "ALLOWED", "2026-03-01T11:00:00Z");
    insertAccessLog(uuid(15), uuid(201), uuid(101), "DENIED", "2026-03-01T08:00:00Z");

    assertEquals(ids(14, 12, 11, 13, 15), getAllAccessLogIdsByPages(null));
    assertEquals(ids(15, 13, 11, 12, 14),
      getAllAccessLogIdsByPages("createdDate=2026-03-01 sortby createdDate/sort.ascending"));
  }

  @Test
  void testGetAccessLogsByDateOnly_inServerZoneBehindUtc_usesUtcDays() throws Throwable {
    prepareReadingRoom();
    inServerZone("America/New_York", () -> {
      // 22:00 on 31 December in New York
      insertAccessLog(uuid(21), uuid(201), uuid(101), "ALLOWED", "2026-01-01T03:00:00Z");
      // 00:30 on 1 January in New York
      insertAccessLog(uuid(22), uuid(201), uuid(101), "ALLOWED", "2026-01-01T05:30:00Z");

      assertEquals(ids(22, 21), getAccessLogIds("createdDate=2026-01-01"));
      assertEquals(List.of(), getAccessLogIds("createdDate=2025-12-31"));
      this.mockMvc.perform(
          get("/reading-room/access-log")
            .param("query", "id==" + uuid(21))
            .headers(defaultHeaders())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessLogs[0].metadata.createdDate").value("2026-01-01T03:00:00.000Z"));
    });
  }

  @Test
  void testGetAccessLogsWithInvalidDateOrQuery_returnsBadRequest() throws Exception {
    prepareReadingRoom();

    for (var query : List.of("createdDate>=2026-13-01", "updatedDate<yesterday", "createdDate>=")) {
      this.mockMvc.perform(
          get("/reading-room/access-log")
            .param("query", query)
            .headers(defaultHeaders())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
    }
  }

  private void insertEntriesAroundJanuary2026() throws Exception {
    prepareReadingRoom();
    insertAccessLog(uuid(1), uuid(201), uuid(101), "ALLOWED", "2025-12-31T23:59:59.999999Z");
    insertAccessLog(uuid(2), uuid(201), uuid(101), "ALLOWED", "2026-01-01T00:00:00Z");
    insertAccessLog(uuid(3), uuid(202), uuid(102), "DENIED", "2026-01-31T23:59:59.999999Z");
    insertAccessLog(uuid(4), uuid(202), uuid(102), "ALLOWED", "2026-02-01T00:00:00Z");
  }

  private void prepareReadingRoom() throws Exception {
    removeReadingRoomIfExists();
    createReadingRoomWithServicePoint(READING_ROOM_ID, SERVICE_POINT_ID1);
  }

  /**
   * Stores an entry created at the given UTC instant the way Spring Data auditing does: as the JVM's local time.
   */
  private void insertAccessLog(UUID id, UUID userId, UUID patronId, String action, String createdAt) {
    var schema = folioModuleMetadata.getDBSchemaName(TENANT);
    var storedCreatedDate = LocalDateTime.ofInstant(Instant.parse(createdAt), ZoneId.systemDefault());
    new JdbcTemplate(dataSource).update("INSERT INTO " + schema + ".access_log "
        + "(id, user_id, patron_id, reading_room_id, reading_room_name, service_point_id, action, created_date) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS timestamp))",
      id, userId, patronId, READING_ROOM_ID, READING_ROOM_NAME, SERVICE_POINT_ID1, action,
      storedCreatedDate.toString());
  }

  private List<String> getAccessLogIds(String query) throws Exception {
    return getAccessLogIds(query, 0, 100);
  }

  private List<String> getAccessLogIds(String query, int offset, int limit) throws Exception {
    var request = get("/reading-room/access-log")
      .param("offset", String.valueOf(offset))
      .param("limit", String.valueOf(limit))
      .headers(defaultHeaders())
      .accept(MediaType.APPLICATION_JSON);
    if (query != null) {
      request.param("query", query);
    }
    var body = this.mockMvc.perform(request)
      .andExpect(status().isOk())
      .andReturn().getResponse().getContentAsString();
    return JsonPath.read(body, "$.accessLogs[*].id");
  }

  private List<String> getAllAccessLogIdsByPages(String query) throws Exception {
    var ids = new ArrayList<String>();
    for (var offset = 0; offset < 10; offset += 2) {
      ids.addAll(getAccessLogIds(query, offset, 2));
    }
    return ids;
  }

  private static UUID uuid(int number) {
    return UUID.fromString(String.format("00000000-0000-0000-0000-%012d", number));
  }

  private static List<String> ids(int... numbers) {
    return Arrays.stream(numbers).mapToObj(number -> uuid(number).toString()).toList();
  }

  private static void inServerZone(String zoneId, Executable action) throws Throwable {
    var originalZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
    try {
      action.execute();
    } finally {
      TimeZone.setDefault(originalZone);
    }
  }

  private void createReadingRoomWithServicePoint(UUID readingRoomId, UUID servicePointId) throws Exception {
    var readingRoom = createReadingRoom(readingRoomId, false);
    readingRoom.servicePoints(Set.of(createServicePoint(servicePointId, SERVICE_POINT_NAME1)));
    this.mockMvc.perform(
        post("/reading-room")
          .content(asJsonString(readingRoom))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());
  }

  private ResultActions postAccessLog(AccessLog accessLog, HttpHeaders headers) throws Exception {
    return this.mockMvc.perform(
      post("/reading-room/" + accessLog.getReadingRoomId() + "/access-log")
        .content(asJsonString(accessLog))
        .headers(headers)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON));
  }

  private static String okapiUserId() {
    return defaultHeaders().getFirst(XOkapiHeaders.USER_ID);
  }

  private void insertLegacyAccessLog(UUID id) {
    var schema = folioModuleMetadata.getDBSchemaName(TENANT);
    new JdbcTemplate(dataSource).update("INSERT INTO " + schema + ".access_log "
        + "(id, user_id, patron_id, reading_room_id, action, created_date) "
        + "VALUES (?, ?, ?, ?, 'ALLOWED', TIMESTAMP '2025-01-15 10:20:30.123456')",
      id, UUID.randomUUID(), UUID.randomUUID(), READING_ROOM_ID);
  }

  private void removeReadingRoomIfExists() {
    runInTenantContext(() -> {
      patronPermissionRepository.deleteAll();
      accessLogRepository.deleteAll();
      readingRoomRepository.deleteAll();
    });
  }

}
