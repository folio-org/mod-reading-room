package org.folio.readingroom.utils;

import static org.folio.readingroom.utils.HelperUtils.canonicalCql;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AccessLogCqlNormalizerTest {

  private static final String NEWEST_FIRST = " sortby createdDate/sort.descending id/sort.descending";
  private static final ZoneId KYIV = ZoneId.of("Europe/Kyiv");

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = "  ")
  void normalize_blankQuery_returnsAllRecordsNewestFirst(String query) {
    assertCql("cql.allRecords=1" + NEWEST_FIRST, normalizeInUtc(query));
  }

  @Test
  void normalize_queryWithoutDateTerms_keepsTermsAndSortsNewestFirst() {
    var query = "(readingRoomName==\"Room \\\"A\\\"\" or readingRoomName=\"*hall*\") and action==ALLOWED";

    assertCql(query + NEWEST_FIRST, normalizeInUtc(query));
  }

  @ParameterizedTest
  @CsvSource(delimiterString = " -> ", textBlock = """
    sortby createdDate/sort.ascending         -> sortby createdDate/sort.ascending id/sort.ascending
    sortby createdDate/sort.descending        -> sortby createdDate/sort.descending id/sort.descending
    sortby updatedDate/sort.descending        -> sortby createdDate/sort.descending id/sort.descending
    sortby updatedDate                        -> sortby createdDate id/sort.ascending
    sortby readingRoomName                    -> sortby readingRoomName id/sort.ascending
    sortby action createdDate/sort.descending -> sortby action createdDate/sort.descending id/sort.ascending
    sortby id/sort.descending                 -> sortby id/sort.descending
    """)
  void normalize_sortClause_sortsUpdatedDateAsCreatedDateAndBreaksTiesById(String sort, String expected) {
    assertCql("cql.allRecords=1 " + expected, normalizeInUtc("cql.allRecords=1 " + sort));
  }

  @ParameterizedTest
  @MethodSource("dateOnlyValues")
  void normalize_dateOnlyValue_coversWholeUtcDay(String query, String expected) {
    assertCql(expected + NEWEST_FIRST, normalizeInUtc(query));
  }

  @ParameterizedTest
  @MethodSource("dateTimeValues")
  void normalize_dateTimeValue_isUtcAndCoversItsLastDigit(String query, String expected) {
    assertCql(expected + NEWEST_FIRST, normalizeInUtc(query));
  }

  @ParameterizedTest
  @MethodSource("kyivStorageValues")
  void normalize_storageZoneAheadOfUtc_shiftsBoundariesToStoredLocalTime(String query, String expected) {
    assertCql(expected + NEWEST_FIRST, AccessLogCqlNormalizer.normalize(query, KYIV));
  }

  @ParameterizedTest
  @CsvSource(delimiterString = " -> ", textBlock = """
    createdDate=""  -> createdDate=""
    updatedDate=""  -> createdDate=""
    """)
  void normalize_emptyDateTerm_onlyRenamesUpdatedDate(String query, String expected) {
    assertCql(expected + NEWEST_FIRST, normalizeInUtc(query));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "createdDate>=2026-13-01",
    "createdDate>=yesterday",
    "createdDate>=2026-01-01T25:00",
    "createdDate>=2026-01-01T10:00:00+0200",
    "createdDate>=2026-01-01:2026-02-01",
    "updatedDate adj 2026-01-01",
    "createdDate>=",
    "(createdDate>=2026-01-01"
  })
  void normalize_invalidQuery_throwsIllegalArgumentException(String query) {
    assertThrows(IllegalArgumentException.class, () -> normalizeInUtc(query));
  }

  static Stream<Arguments> dateOnlyValues() {
    return Stream.of(
      arguments("createdDate>=2026-01-01", "createdDate>=2026-01-01T00:00:00"),
      arguments("createdDate>2026-01-01", "createdDate>=2026-01-02T00:00:00"),
      arguments("createdDate<2026-01-01", "createdDate<2026-01-01T00:00:00"),
      arguments("createdDate<=2026-01-31", "createdDate<2026-02-01T00:00:00"),
      arguments("createdDate<=2026-12-31", "createdDate<2027-01-01T00:00:00"),
      arguments("createdDate=2028-02-29", "createdDate>=2028-02-29T00:00:00 and createdDate<2028-03-01T00:00:00"),
      arguments("createdDate==2026-01-15", "createdDate>=2026-01-15T00:00:00 and createdDate<2026-01-16T00:00:00"),
      arguments("createdDate<>2026-01-15", "createdDate<2026-01-15T00:00:00 or createdDate>=2026-01-16T00:00:00"),
      arguments("createdDate=2026-01-01:2026-02-01",
        "createdDate>=2026-01-01T00:00:00 and createdDate<2026-02-01T00:00:00"),
      arguments("createdDate>=2026-01-01 and createdDate<2026-02-01",
        "createdDate>=2026-01-01T00:00:00 and createdDate<2026-02-01T00:00:00"),
      arguments("updatedDate>=2026-01-01 and updatedDate<2026-02-01",
        "createdDate>=2026-01-01T00:00:00 and createdDate<2026-02-01T00:00:00"),
      arguments("patronId==1b2f6a3e-0c4d-4e5f-8a9b-0c1d2e3f4a5b and createdDate=2026-01-15",
        "patronId==1b2f6a3e-0c4d-4e5f-8a9b-0c1d2e3f4a5b"
          + " and (createdDate>=2026-01-15T00:00:00 and createdDate<2026-01-16T00:00:00)")
    );
  }

  static Stream<Arguments> dateTimeValues() {
    return Stream.of(
      // the date range the FOLIO UI builds: zone-less UTC values with milliseconds
      arguments("createdDate>=\"2026-01-01T00:00:00.000\" and createdDate<=\"2026-01-31T23:59:59.999\"",
        "createdDate>=2026-01-01T00:00:00 and createdDate<2026-02-01T00:00:00"),
      arguments("createdDate==2026-01-15T10:20:30.123",
        "createdDate>=2026-01-15T10:20:30.123 and createdDate<2026-01-15T10:20:30.124"),
      arguments("createdDate==2026-01-15T10:20:30.123456",
        "createdDate>=2026-01-15T10:20:30.123456 and createdDate<2026-01-15T10:20:30.123457"),
      arguments("createdDate<=2026-01-15T10:20:30", "createdDate<2026-01-15T10:20:31"),
      arguments("createdDate<=2026-01-15T10:20", "createdDate<2026-01-15T10:21:00"),
      arguments("createdDate>2026-01-15T10:20:30.123Z", "createdDate>=2026-01-15T10:20:30.124"),
      arguments("createdDate<2026-01-01T02:00:00+02:00", "createdDate<2026-01-01T00:00:00"),
      arguments("createdDate>=2025-12-31T19:00:00-05:00", "createdDate>=2026-01-01T00:00:00")
    );
  }

  static Stream<Arguments> kyivStorageValues() {
    return Stream.of(
      // winter time, UTC+2
      arguments("createdDate=2025-12-31", "createdDate>=2025-12-31T02:00:00 and createdDate<2026-01-01T02:00:00"),
      // summer time, UTC+3
      arguments("createdDate>=2026-07-01", "createdDate>=2026-07-01T03:00:00"),
      // the clocks go forward at 01:00 UTC on 2026-03-29, so that UTC day ends at 03:00 local time
      arguments("createdDate=2026-03-29", "createdDate>=2026-03-29T02:00:00 and createdDate<2026-03-30T03:00:00"),
      arguments("createdDate>=2026-01-15T10:00:00Z", "createdDate>=2026-01-15T12:00:00")
    );
  }

  private static String normalizeInUtc(String query) {
    return AccessLogCqlNormalizer.normalize(query, ZoneOffset.UTC);
  }

  private static void assertCql(String expected, String actual) {
    assertEquals(canonicalCql(expected), canonicalCql(actual));
  }
}
