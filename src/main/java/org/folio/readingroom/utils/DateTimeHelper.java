package org.folio.readingroom.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeHelper {

  private static final DateTimeFormatter UTC_ISO_MILLIS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

  /**
   * Reads the value as local time in the given zone and renders it as an ISO 8601 UTC string
   * with three fraction digits, for example {@code 2026-10-09T14:03:22.123Z}.
   */
  public static String toUtcIsoString(LocalDateTime value, ZoneId sourceZone) {
    if (value == null) {
      return null;
    }
    return UTC_ISO_MILLIS.format(value.atZone(sourceZone).withZoneSameInstant(ZoneOffset.UTC));
  }

  /**
   * Same as {@link #toUtcIsoString(LocalDateTime, ZoneId)}, reading the value in the JVM's default zone,
   * which is the zone Spring Data auditing stamps {@link LocalDateTime} values in.
   */
  public static String toUtcIsoString(LocalDateTime value) {
    return toUtcIsoString(value, ZoneId.systemDefault());
  }
}
