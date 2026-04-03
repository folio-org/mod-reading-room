package org.folio.readingroom.config;

import org.folio.readingroom.client.ReadingRoomUsersClient;
import org.folio.readingroom.client.ServicePointClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class HttpClientConfiguration {

  @Bean
  public ReadingRoomUsersClient readingRoomUsersClient(HttpServiceProxyFactory factory) {
    return factory.createClient(ReadingRoomUsersClient.class);
  }

  @Bean
  public ServicePointClient servicePointClient(HttpServiceProxyFactory factory) {
    return factory.createClient(ServicePointClient.class);
  }
}
