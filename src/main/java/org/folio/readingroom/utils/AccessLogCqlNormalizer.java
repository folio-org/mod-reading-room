package org.folio.readingroom.utils;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.z3950.zing.cql.CQLAndNode;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLNotNode;
import org.z3950.zing.cql.CQLOrNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;
import org.z3950.zing.cql.CQLRelation;
import org.z3950.zing.cql.CQLSortNode;
import org.z3950.zing.cql.CQLTermNode;
import org.z3950.zing.cql.Modifier;
import org.z3950.zing.cql.ModifierSet;

/**
 * Adapts a CQL query of {@code GET /reading-room/access-log} to the CQL repository:
 * <ul>
 *   <li>{@code updatedDate} is searched and sorted as {@code createdDate}, since entries are never updated;</li>
 *   <li>dates without an offset are UTC, and a value covers its last unit: {@code 2026-01-31} is the whole day;</li>
 *   <li>the default sort is newest first, and {@code id} is always the last sort key, so paging is stable.</li>
 * </ul>
 * Invalid queries and dates throw {@link IllegalArgumentException}.
 */
@UtilityClass
public class AccessLogCqlNormalizer {

  private static final String ALL_RECORDS = "cql.allRecords=1";
  private static final String CREATED_DATE = "createdDate";
  private static final String UPDATED_DATE = "updatedDate";
  private static final String ID = "id";
  private static final String SORT_ASCENDING = "sort.ascending";
  private static final String SORT_DESCENDING = "sort.descending";
  private static final Pattern DATE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
  private static final Pattern DATE_RANGE = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}):(\\d{4}-\\d{2}-\\d{2})");
  private static final Pattern DATE_TIME = Pattern.compile("(?<local>\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}"
    + "(?<seconds>:\\d{2}(?<fraction>\\.\\d{1,9})?)?)(?<offset>Z|[+-]\\d{2}:\\d{2})?");

  /**
   * Same as {@link #normalize(String, ZoneId)} for dates stored in the JVM's default zone, which is the zone
   * Spring Data auditing stamps them in (see {@link DateTimeHelper#toUtcIsoString(LocalDateTime)}).
   */
  public static String normalize(String query) {
    return normalize(query, ZoneId.systemDefault());
  }

  public static String normalize(String query, ZoneId storageZone) {
    var root = parse(query == null || query.isBlank() ? ALL_RECORDS : query);
    var subtree = root instanceof CQLSortNode sortNode ? sortNode.getSubtree() : root;
    var sortKeys = root instanceof CQLSortNode sortNode ? sortNode.getSortIndexes() : List.<ModifierSet>of();
    var result = new CQLSortNode(rewrite(subtree, storageZone));
    withTieBreaker(sortKeys).forEach(result::addSortIndex);
    return result.toCQL();
  }

  private static CQLNode parse(String query) {
    try {
      return new CQLParser().parse(query);
    } catch (CQLParseException | IOException e) {
      throw new IllegalArgumentException("Invalid CQL query: " + e.getMessage(), e);
    }
  }

  private static CQLNode rewrite(CQLNode node, ZoneId storageZone) {
    return switch (node) {
      case CQLTermNode term -> rewriteTerm(term, storageZone);
      case CQLAndNode and -> new CQLAndNode(rewrite(and.getLeftOperand(), storageZone),
        rewrite(and.getRightOperand(), storageZone), modifierSet("and", and.getModifiers()));
      case CQLOrNode or -> new CQLOrNode(rewrite(or.getLeftOperand(), storageZone),
        rewrite(or.getRightOperand(), storageZone), modifierSet("or", or.getModifiers()));
      case CQLNotNode not -> new CQLNotNode(rewrite(not.getLeftOperand(), storageZone),
        rewrite(not.getRightOperand(), storageZone), modifierSet("not", not.getModifiers()));
      default -> node;
    };
  }

  private static CQLNode rewriteTerm(CQLTermNode term, ZoneId storageZone) {
    if (!CREATED_DATE.equals(term.getIndex()) && !UPDATED_DATE.equals(term.getIndex())) {
      return term;
    }
    var comparator = term.getRelation().getBase();
    if (!term.getRelation().getModifiers().isEmpty() || term.getTerm().isEmpty() && isEquality(comparator)) {
      return new CQLTermNode(CREATED_DATE, term.getRelation(), term.getTerm());
    }
    var period = toPeriod(term);
    var start = toStored(period.start(), storageZone);
    var end = toStored(period.end(), storageZone);
    return switch (comparator) {
      case ">=" -> createdDate(">=", start);
      case ">" -> createdDate(">=", end);
      case "<" -> createdDate("<", start);
      case "<=" -> createdDate("<", end);
      case "=", "==" -> new CQLAndNode(createdDate(">=", start), createdDate("<", end), new ModifierSet("and"));
      case "<>" -> new CQLOrNode(createdDate("<", start), createdDate(">=", end), new ModifierSet("or"));
      default -> throw new IllegalArgumentException(String.format(
        "Relation '%s' is not supported for %s, use =, ==, <>, <, <=, > or >=", comparator, term.getIndex()));
    };
  }

  private static Period toPeriod(CQLTermNode term) {
    var value = term.getTerm();
    try {
      var range = DATE_RANGE.matcher(value);
      if (range.matches() && isEquality(term.getRelation().getBase())) {
        return new Period(startOfUtcDay(range.group(1)), startOfUtcDay(range.group(2)));
      }
      if (DATE.matcher(value).matches()) {
        var start = startOfUtcDay(value);
        return new Period(start, start.plus(Duration.ofDays(1)));
      }
      var dateTime = DATE_TIME.matcher(value);
      if (dateTime.matches()) {
        var start = dateTime.group("offset") == null
          ? LocalDateTime.parse(dateTime.group("local")).toInstant(ZoneOffset.UTC)
          : OffsetDateTime.parse(value).toInstant();
        return new Period(start, start.plus(lastDigitUnit(dateTime)));
      }
    } catch (DateTimeException e) {
      throw invalidDate(term, e);
    }
    throw invalidDate(term, null);
  }

  private static Instant startOfUtcDay(String date) {
    return LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant();
  }

  private static Duration lastDigitUnit(Matcher dateTime) {
    var fraction = dateTime.group("fraction");
    if (fraction != null) {
      var digits = fraction.length() - 1;
      return Duration.ofNanos((long) Math.pow(10, 9 - digits));
    }
    return dateTime.group("seconds") != null ? Duration.ofSeconds(1) : Duration.ofMinutes(1);
  }

  private static String toStored(Instant instant, ZoneId storageZone) {
    return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(instant, storageZone));
  }

  private static CQLTermNode createdDate(String comparator, String value) {
    return new CQLTermNode(CREATED_DATE, new CQLRelation(comparator), value);
  }

  private static boolean isEquality(String comparator) {
    return "=".equals(comparator) || "==".equals(comparator);
  }

  private static IllegalArgumentException invalidDate(CQLTermNode term, DateTimeException cause) {
    return new IllegalArgumentException(String.format(
      "Invalid date '%s' for %s, use yyyy-MM-dd or an ISO 8601 date-time", term.getTerm(), term.getIndex()), cause);
  }

  private static List<ModifierSet> withTieBreaker(List<ModifierSet> requested) {
    var keys = new ArrayList<ModifierSet>();
    requested.forEach(key -> keys.add(UPDATED_DATE.equals(key.getBase())
      ? modifierSet(CREATED_DATE, key.getModifiers()) : key));
    if (keys.isEmpty()) {
      keys.add(sortKey(CREATED_DATE, SORT_DESCENDING));
    }
    if (keys.stream().noneMatch(key -> ID.equals(key.getBase()))) {
      keys.add(sortKey(ID, isDescending(keys.getFirst()) ? SORT_DESCENDING : SORT_ASCENDING));
    }
    return keys;
  }

  private static boolean isDescending(ModifierSet sortKey) {
    return sortKey.getModifiers().stream().anyMatch(modifier -> SORT_DESCENDING.equalsIgnoreCase(modifier.getType()));
  }

  private static ModifierSet sortKey(String index, String direction) {
    var key = new ModifierSet(index);
    key.addModifier(direction);
    return key;
  }

  private static ModifierSet modifierSet(String base, List<Modifier> modifiers) {
    var set = new ModifierSet(base);
    for (var modifier : modifiers) {
      if (modifier.getValue() == null) {
        set.addModifier(modifier.getType());
      } else {
        set.addModifier(modifier.getType(), modifier.getComparison(), modifier.getValue());
      }
    }
    return set;
  }

  private record Period(Instant start, Instant end) {
  }
}
