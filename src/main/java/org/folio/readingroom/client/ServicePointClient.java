package org.folio.readingroom.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.folio.spring.model.ResultList;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "service-points", contentType = MediaType.APPLICATION_JSON_VALUE)
public interface ServicePointClient {

  @GetExchange("?query=id==({servicePointId})")
  ResultList<InventoryServicePoint> getServicePointsByIds(@PathVariable("servicePointId") String servicePointId);

  @JsonIgnoreProperties(ignoreUnknown = true)
  record InventoryServicePoint(String id, String name) {}
}
