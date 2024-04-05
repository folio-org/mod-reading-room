package org.folio.readingroom.exception;

import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class ServicePointException extends RuntimeException {

  private final List<UUID> invalidIds;

  public ServicePointException(String errorMsg, List<UUID> invalidIds) {
    super(errorMsg);
    this.invalidIds = invalidIds;
  }

}
