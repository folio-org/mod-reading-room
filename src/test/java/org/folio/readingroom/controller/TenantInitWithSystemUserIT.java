package org.folio.readingroom.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
  "folio.system-user.enabled=true",
  "folio.system-user.username=mod-reading-room-system-user",
  "folio.system-user.password=system-user-password",
  "folio.system-user.permissions-file-path=permissions/system-user-permissions.csv",
})
class TenantInitWithSystemUserIT extends BaseIT {

  @Test
  void initializeTenant_positive_systemUserEnabled() throws Exception {
    wireMockServer.resetAll();
    stubSystemUserEndpoints();

    mockMvc.perform(post("/_/tenant")
        .content(asJsonString(new TenantAttributes().moduleTo("mod-reading-room")))
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON))
      .andExpect(status().isNoContent());

    wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/users")));
    wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/authn/credentials")));

    mockMvc.perform(post("/_/tenant")
        .content(asJsonString(new TenantAttributes().purge(true)))
        .headers(defaultHeaders())
        .contentType(APPLICATION_JSON))
      .andExpect(status().isNoContent());
  }

  private void stubSystemUserEndpoints() {
    wireMockServer.stubFor(get(urlPathEqualTo("/users"))
      .willReturn(aResponse()
        .withStatus(HttpStatus.OK.value())
        .withHeader("Content-Type", "application/json")
        .withBody("""
          {"users": [], "totalRecords": 0}
          """)));

    wireMockServer.stubFor(WireMock.post(urlPathEqualTo("/users"))
      .willReturn(aResponse()
        .withStatus(HttpStatus.CREATED.value())
        .withHeader("Content-Type", "application/json")
        .withBody("""
          {
            "id": "11111111-1111-1111-1111-111111111111",
            "username": "mod-reading-room-system-user",
            "active": true
          }
          """)));

    wireMockServer.stubFor(get(urlPathEqualTo("/perms/users/11111111-1111-1111-1111-111111111111/permissions"))
      .willReturn(aResponse()
        .withStatus(HttpStatus.NOT_FOUND.value())));

    wireMockServer.stubFor(WireMock.post(urlPathEqualTo("/perms/users"))
      .willReturn(aResponse()
        .withStatus(HttpStatus.CREATED.value())));

    wireMockServer.stubFor(WireMock.post(urlPathEqualTo("/authn/credentials"))
      .willReturn(aResponse()
        .withStatus(HttpStatus.CREATED.value())));

    wireMockServer.stubFor(WireMock.post(urlPathEqualTo("/authn/login-with-expiry"))
      .willReturn(aResponse()
        .withStatus(HttpStatus.CREATED.value())
        .withHeader("Content-Type", "application/json")
        .withHeader("Set-Cookie",
          "folioAccessToken=test-access-token; Max-Age=600; Path=/; Secure; HttpOnly; SameSite=None")
        .withHeader("Set-Cookie",
          "folioRefreshToken=test-refresh-token; Max-Age=604800; Path=/; Secure; HttpOnly; SameSite=None")
        .withBody("""
          {
            "accessTokenExpiration": "2099-01-01T00:00:00Z",
            "refreshTokenExpiration": "2099-01-01T00:00:00Z"
          }
          """)));
  }
}
