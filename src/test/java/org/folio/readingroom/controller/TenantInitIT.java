package org.folio.readingroom.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TenantInitIT extends BaseIT {

  @BeforeEach
  void resetWireMock() {
    wireMockServer.resetAll();
  }

  @AfterEach
  void purgeTenant() throws Exception {
    mockMvc.perform(post("/_/tenant")
        .content(asJsonString(new TenantAttributes()
          .moduleTo("mod-reading-room")
          .purge(true)))
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }

  @Test
  void initializeTenant_positive_systemUserDisabled() throws Exception {
    mockMvc.perform(post("/_/tenant")
        .content(asJsonString(new TenantAttributes().moduleTo("mod-reading-room")))
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON))
      .andExpect(status().isNoContent());

    wireMockServer.verify(0, postRequestedFor(urlPathEqualTo("/authn/credentials")));
    wireMockServer.verify(0, postRequestedFor(urlPathEqualTo("/users")));
  }

  @Test
  void initializeTenant_positive_repeatedInitialization() throws Exception {
    mockMvc.perform(post("/_/tenant")
        .content(asJsonString(new TenantAttributes().moduleTo("mod-reading-room")))
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON))
      .andExpect(status().isNoContent());

    mockMvc.perform(post("/_/tenant")
        .content(asJsonString(new TenantAttributes().moduleTo("mod-reading-room")))
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }
}
