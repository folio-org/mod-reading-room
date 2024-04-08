package org.folio.readingroom.client.feign;

import org.folio.spring.config.FeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "readingRoom-users-client", url = "users", configuration = FeignClientConfiguration.class)
public interface UsersClient {
  @GetMapping(value = "/{userId}")
  void getUserById(@PathVariable String userId);
}
