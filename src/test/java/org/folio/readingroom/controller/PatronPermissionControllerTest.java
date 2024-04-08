package org.folio.readingroom.controller;

import static org.folio.readingroom.utils.HelperUtils.createPatronPermission;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.UUID;
import org.folio.readingroom.client.feign.UsersClient;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.repository.PatonPermissionsRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.spring.service.SystemUserScopedExecutionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

class PatronPermissionControllerTest extends BaseIT {

  @MockBean
  UsersClient usersClient;
  @MockBean
  private PatonPermissionsRepository patonPermissionsRepository;
  @Autowired
  private ReadingRoomRepository readingRoomRepository;
  @Autowired
  private SystemUserScopedExecutionService systemUserScopedExecutionService;

  @Test
  void testSuccessUpdatePatronPermission() throws Exception {
    PatronPermission patronPermission = createPatronPermission(UUID.randomUUID(),
      UUID.randomUUID(), UUID.fromString("2205005b-ca51-4a04-87fd-938eefa8f6df"));
    doNothing().when(usersClient).getUserById("2205005b-ca51-4a04-87fd-938eefa8f6df");
    when(patonPermissionsRepository.saveAll(anyList()))
      .thenReturn(Collections.singletonList(new PatronPermissionEntity()));
    this.mockMvc.perform(
        put("/reading-room-patron-permission/2205005b-ca51-4a04-87fd-938eefa8f6df")
          .content(asJsonString(patronPermission))
          .headers(defaultHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isCreated());
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
}
