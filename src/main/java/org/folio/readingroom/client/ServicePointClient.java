package org.folio.readingroom.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.folio.spring.model.ResultList;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "service-points", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface ServicePointClient {

  @GetExchange
  ResultList<InventoryServicePoint> getServicePointsByIds(@RequestParam("query") String query);

  @JsonIgnoreProperties(ignoreUnknown = true)
  record InventoryServicePoint(String id, String name) {}
}
