package org.folio.readingroom.client.feign;

import okhttp3.ConnectionPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Conf {

  @Bean
  public okhttp3.OkHttpClient okHttpClient() {
    return new okhttp3.OkHttpClient.Builder()
      .connectionPool(new ConnectionPool(10, 5000, java.util.concurrent.TimeUnit.MILLISECONDS))
      .build();
  }
}
