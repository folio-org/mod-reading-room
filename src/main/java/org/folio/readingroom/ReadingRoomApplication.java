package org.folio.readingroom;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReadingRoomApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReadingRoomApplication.class, args);
  }

}
