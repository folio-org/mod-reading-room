package org.folio.readingroom.service.converter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.AccessLogCollection;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ReadingRoomCollection;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.AccessLogEntity;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.folio.readingroom.domain.projection.PatronPermissionProjection;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

@org.mapstruct.Mapper(componentModel = "spring",
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  unmappedTargetPolicy = ReportingPolicy.IGNORE,
  builder = @Builder(disableBuilder = true))
public interface Mapper {

  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  @Mapping(target = "readingRoom.id", source = "patronPermission.readingRoomId")
  PatronPermissionEntity toEntity(PatronPermission patronPermission);

  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  ReadingRoomEntity toEntity(ReadingRoom readingRoom);

  @Mapping(target = "id", source = "value")
  @Mapping(target = "name", source = "label")
  ReadingRoomServicePointEntity toEntity(ServicePoint servicePoint);

  AccessLogEntity toEntity(AccessLog accessLog);

  List<ReadingRoom> toDtoList(List<ReadingRoomEntity> readingRoomEntity);

  List<AccessLog> toDtoAccessLogList(List<AccessLogEntity> accessLogEntities);

  @Mapping(target = "value", source = "id")
  @Mapping(target = "label", source = "name")
  ServicePoint toDto(ReadingRoomServicePointEntity servicePointEntity);

  AccessLog toDto(AccessLogEntity entity);

  @Mapping(target = "metadata.createdByUserId", source = "createdBy")
  @Mapping(target = "metadata.createdDate", source = "createdDate")
  @Mapping(target = "metadata.updatedByUserId", source = "updatedBy")
  @Mapping(target = "metadata.updatedDate", source = "updatedDate")
  ReadingRoom toDto(ReadingRoomEntity readingRoomEntity);

  @Mapping(target = "metadata.createdByUserId", source = "createdBy")
  @Mapping(target = "metadata.createdDate", source = "createdDate")
  @Mapping(target = "metadata.updatedByUserId", expression = "java(setUpdatedByUserId(patronPermissionProjection))")
  @Mapping(target = "metadata.updatedDate", expression = "java(setUpdatedDate(patronPermissionProjection))")
  PatronPermission toDto(PatronPermissionProjection patronPermissionProjection);

  default ReadingRoomCollection toDto(Page<ReadingRoomEntity> readingRoomEntityPage, boolean includeDeleted) {
    var readingRooms = readingRoomEntityPage.getContent();
    if (!includeDeleted) {
      readingRooms = readingRooms
        .stream()
        .filter(readingRoom -> !readingRoom.isDeleted())
        .toList();
    }
    return new ReadingRoomCollection(toDtoList(readingRooms), readingRooms.size());
  }

  default AccessLogCollection toDtoCollection(Page<AccessLogEntity> accessLogEntitiesPage) {
    var accessLogEntities = toDtoAccessLogList(accessLogEntitiesPage.getContent());
    return new AccessLogCollection(accessLogEntities, (int) accessLogEntitiesPage.getTotalElements());
  }

  default String setUpdatedDate(PatronPermissionProjection patronPermissionProjection) {
    return patronPermissionProjection.getUpdatedDate() != null
      ? patronPermissionProjection.getUpdatedDate() : patronPermissionProjection.getCreatedDate();
  }

  default UUID setUpdatedByUserId(PatronPermissionProjection patronPermissionProjection) {
    return patronPermissionProjection.getUpdatedBy() != null
      ? patronPermissionProjection.getUpdatedBy() : patronPermissionProjection.getCreatedBy();
  }

  @AfterMapping
  default void setReadingRoom(@MappingTarget ReadingRoomEntity readingRoomEntity) {
    Optional.ofNullable(readingRoomEntity.getServicePoints())
      .ifPresent(spEntities -> spEntities.forEach(spEntity -> spEntity.setReadingRoom(readingRoomEntity)));
  }

  List<PatronPermissionEntity> toEntityList(List<PatronPermission> patronPermissions);
}
