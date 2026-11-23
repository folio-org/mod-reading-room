package org.folio.readingroom.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DateTimeHelperTest {

  @ParameterizedTest
  @CsvSource({
    "2026-10-09T14:03:22.123,       UTC,              2026-10-09T14:03:22.123Z",
    "2026-10-09T17:03:22.123,       Europe/Kyiv,      2026-10-09T14:03:22.123Z",
    "2026-10-09T10:03:22.123,       America/New_York, 2026-10-09T14:03:22.123Z",
    "2026-10-10T01:30:00,           Europe/Kyiv,      2026-10-09T22:30:00.000Z",
    "2026-10-09T14:03:22.123999999, UTC,              2026-10-09T14:03:22.123Z",
    "2026-10-09T14:03:22,           UTC,              2026-10-09T14:03:22.000Z",
    "2026-03-29T03:30:00,           Europe/Kyiv,      2026-03-29T01:30:00.000Z",
    "2026-10-25T03:30:00,           Europe/Kyiv,      2026-10-25T00:30:00.000Z"
  })
  void toUtcIsoString_convertsStoredLocalTimeToUtc(LocalDateTime stored, String zone, String expected) {
    assertEquals(expected, DateTimeHelper.toUtcIsoString(stored, ZoneId.of(zone)));
  }

  @Test
  void toUtcIsoString_null_returnsNull() {
    assertNull(DateTimeHelper.toUtcIsoString(null, ZoneId.of("UTC")));
    assertNull(DateTimeHelper.toUtcIsoString(null));
  }

  @Test
  void toUtcIsoString_withoutZone_usesJvmDefaultZone() {
    var original = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    try {
      assertEquals("2026-10-09T14:03:22.123Z",
        DateTimeHelper.toUtcIsoString(LocalDateTime.of(2026, 10, 9, 23, 3, 22, 123_000_000)));
    } finally {
      TimeZone.setDefault(original);
    }
  }
}
