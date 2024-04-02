package org.folio.readingroom.util;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CqlHelper {
  public static String getAnyMatch(List<UUID> uuidList) {
    return uuidList
      .stream()
      .filter(Objects::nonNull)
      .map(UUID::toString)
      .collect(Collectors.joining(" or "));
  }

}
