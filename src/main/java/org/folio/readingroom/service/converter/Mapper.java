package org.folio.readingroom.service.converter;

import java.util.List;
import java.util.Optional;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.PatronPermission;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ReadingRoomCollection;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.AccessLogEntity;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
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

  ReadingRoomServicePointEntity toEntity(ServicePoint servicePoint);

  @Mapping(target = "readingRoom.id", source = "accessLog.readingRoomId")
  AccessLogEntity toEntity(AccessLog accessLog);

  List<ReadingRoom> toDtoList(List<ReadingRoomEntity> readingRoomEntity);

  ServicePoint toDto(ReadingRoomServicePointEntity servicePointEntity);

  @Mapping(target = "readingRoomId", source = "entity.readingRoom.id")
  AccessLog toDto(AccessLogEntity entity);

  @Mapping(target = "metadata.createdByUserId", source = "createdBy")
  @Mapping(target = "metadata.createdDate", source = "createdDate")
  @Mapping(target = "metadata.updatedByUserId", source = "updatedBy")
  @Mapping(target = "metadata.updatedDate", source = "updatedDate")
  ReadingRoom toDto(ReadingRoomEntity readingRoomEntity);

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

  @AfterMapping
  default void setReadingRoom(@MappingTarget ReadingRoomEntity readingRoomEntity) {
    Optional.ofNullable(readingRoomEntity.getServicePoints())
      .ifPresent(spEntities -> spEntities.forEach(spEntity -> spEntity.setReadingRoom(readingRoomEntity)));
  }

  List<PatronPermissionEntity> toEntityList(List<PatronPermission> patronPermissions);
}
