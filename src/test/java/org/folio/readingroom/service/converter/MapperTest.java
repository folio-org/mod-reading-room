package org.folio.readingroom.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.Metadata;
import org.folio.readingroom.domain.entity.AccessLogEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class MapperTest {

  private static final LocalDateTime CREATED = LocalDateTime.of(2025, 1, 15, 10, 20, 30, 123_456_000);
  private static final LocalDateTime UPDATED = LocalDateTime.of(2025, 2, 1, 8, 0, 0);
  private static final UUID CREATOR = UUID.randomUUID();
  private static final UUID UPDATER = UUID.randomUUID();

  private final Mapper mapper = Mappers.getMapper(Mapper.class);
  private TimeZone originalZone;

  @BeforeEach
  void pinServerZoneToUtc() {
    originalZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @AfterEach
  void restoreServerZone() {
    TimeZone.setDefault(originalZone);
  }

  @Test
  void toDto_mapsAuditFieldsToMetadata() {
    var metadata = mapper.toDto(entity(CREATED, CREATOR, UPDATED, UPDATER)).getMetadata();

    assertEquals("2025-01-15T10:20:30.123Z", metadata.getCreatedDate());
    assertEquals(CREATOR, metadata.getCreatedByUserId());
    assertEquals("2025-02-01T08:00:00.000Z", metadata.getUpdatedDate());
    assertEquals(UPDATER, metadata.getUpdatedByUserId());
  }

  @Test
  void toDto_neverUpdated_updatedFieldsEqualCreatedFields() {
    var metadata = mapper.toDto(entity(CREATED, CREATOR, null, null)).getMetadata();

    assertEquals("2025-01-15T10:20:30.123Z", metadata.getUpdatedDate());
    assertEquals(CREATOR, metadata.getUpdatedByUserId());
  }

  @Test
  void toDto_legacyEntityWithoutUserNameAndServicePoint_mapsWithoutError() {
    var legacy = entity(CREATED, null, null, null);
    legacy.setReadingRoomName(null);
    legacy.setServicePointId(null);

    var dto = mapper.toDto(legacy);

    assertNull(dto.getReadingRoomName());
    assertNull(dto.getServicePointId());
    assertEquals("2025-01-15T10:20:30.123Z", dto.getMetadata().getUpdatedDate());
    assertNull(dto.getMetadata().getCreatedByUserId());
    assertNull(dto.getMetadata().getUpdatedByUserId());
  }

  @Test
  void toDtoCollection_everyItemHasMetadata_totalRecordsComesFromPage() {
    var page = new PageImpl<>(List.of(entity(CREATED, CREATOR, null, null), entity(UPDATED, null, null, null)),
      PageRequest.of(0, 2), 5);

    var collection = mapper.toDtoCollection(page);

    assertEquals(5, collection.getTotalRecords());
    collection.getAccessLogs().forEach(accessLog -> assertNotNull(accessLog.getMetadata().getCreatedDate()));
  }

  @Test
  void toEntity_ignoresClientSuppliedMetadata() {
    var accessLog = new AccessLog().id(UUID.randomUUID())
      .metadata(new Metadata().createdDate("2000-01-01T00:00:00.000Z").createdByUserId(UUID.randomUUID())
        .updatedDate("2000-01-02T00:00:00.000Z").updatedByUserId(UUID.randomUUID()));

    var entity = mapper.toEntity(accessLog);

    assertNull(entity.getCreatedDate());
    assertNull(entity.getCreatedBy());
    assertNull(entity.getUpdatedDate());
    assertNull(entity.getUpdatedBy());
  }

  private AccessLogEntity entity(LocalDateTime created, UUID createdBy, LocalDateTime updated, UUID updatedBy) {
    var entity = new AccessLogEntity();
    entity.setId(UUID.randomUUID());
    entity.setReadingRoomName("Room");
    entity.setServicePointId(UUID.randomUUID());
    entity.setCreatedDate(created);
    entity.setCreatedBy(createdBy);
    entity.setUpdatedDate(updated);
    entity.setUpdatedBy(updatedBy);
    return entity;
  }
}
