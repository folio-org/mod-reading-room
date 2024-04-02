package org.folio.readingroom.client.feign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.folio.spring.config.FeignClientConfiguration;
import org.folio.spring.model.ResultList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-points", configuration = FeignClientConfiguration.class)
public interface InventoryServicePointClient {

  @GetMapping("?query=id==({servicePointIds})")
  ResultList<InventoryServicePoint> getServicePointByName(@PathVariable("servicePointIds") String servicePointIds);

  @JsonIgnoreProperties(ignoreUnknown = true)
  record InventoryServicePoint(String id, String name){}
}
