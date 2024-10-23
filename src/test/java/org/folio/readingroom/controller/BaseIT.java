package org.folio.readingroom.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import org.folio.spring.config.properties.FolioEnvironment;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = BaseIT.DockerPostgresDataSourceInitializer.class)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class BaseIT {
  public static WireMockServer wireMockServer;
  public static final String TENANT = "diku";
  public static final int WIRE_MOCK_PORT = TestSocketUtils.findAvailableTcpPort();
  protected static final String TOKEN = "test_token";
  protected static PostgreSQLContainer<?> postgreDBContainer = new PostgreSQLContainer<>(Objects
    .toString(System.getenv("TESTCONTAINERS_POSTGRES_IMAGE"), "postgres:16-alpine"));
  protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_NULL)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
  @Autowired
  protected MockMvc mockMvc;

  static {
    postgreDBContainer.start();
  }

  @BeforeAll
  static void beforeAll(@Autowired MockMvc mockMvc, @Autowired FolioEnvironment folioEnvironment) {
    wireMockServer = new WireMockServer(WIRE_MOCK_PORT);
    wireMockServer.start();
    setUpTenant(mockMvc);
    folioEnvironment.setOkapiUrl(getOkapiUrl());
  }

  public static String getOkapiUrl() {
    return String.format("http://localhost:%s", WIRE_MOCK_PORT);
  }

  @AfterAll
  static void tearDown() {
    wireMockServer.stop();
  }

  @SneakyThrows
  protected static void setUpTenant(MockMvc mockMvc) {
    mockMvc.perform(post("/_/tenant")
      .content(asJsonString(new TenantAttributes().moduleTo("mod-reading-room")))
      .headers(defaultHeaders())
      .contentType(APPLICATION_JSON)).andExpect(status().isNoContent());
  }

  public static HttpHeaders defaultHeaders() {
    final HttpHeaders httpHeaders = new HttpHeaders();

    httpHeaders.setContentType(APPLICATION_JSON);
    httpHeaders.put(XOkapiHeaders.TENANT, List.of(TENANT));
    httpHeaders.add(XOkapiHeaders.URL, wireMockServer.baseUrl());
    httpHeaders.add(XOkapiHeaders.TOKEN, TOKEN);
    httpHeaders.add(XOkapiHeaders.USER_ID, "08d51c7a-0f36-4f3d-9e35-d285612a23df");

    return httpHeaders;
  }

  @SneakyThrows
  public static String asJsonString(Object value) {
    return OBJECT_MAPPER.writeValueAsString(value);
  }

  public static class DockerPostgresDataSourceInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
        "spring.datasource.url=" + postgreDBContainer.getJdbcUrl(),
        "spring.datasource.username=" + postgreDBContainer.getUsername(),
        "spring.datasource.password=" + postgreDBContainer.getPassword());
    }
  }

}
