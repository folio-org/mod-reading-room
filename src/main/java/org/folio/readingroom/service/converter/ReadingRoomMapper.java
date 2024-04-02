package org.folio.readingroom.service.converter;

import java.util.Optional;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  builder = @Builder(disableBuilder = true))
public interface ReadingRoomMapper {
  ReadingRoomEntity toEntity(ReadingRoom readingRoom);

  @Mapping(target = "servicePointId", source = "servicePoint.id")
  @Mapping(target = "servicePointName", source = "servicePoint.name")
  ReadingRoomServicePointEntity toEntity(ServicePoint servicePoint);

  @Mapping(target = "id", source = "servicePointEntity.servicePointId")
  @Mapping(target = "name", source = "servicePointEntity.servicePointName")
  ServicePoint toDto(ReadingRoomServicePointEntity servicePointEntity);

  org.folio.readingroom.domain.dto.ReadingRoom toDto(ReadingRoomEntity readingRoomEntity);

  @AfterMapping
  default void setReadingRoom(@MappingTarget ReadingRoomEntity readingRoomEntity) {
    Optional.ofNullable(readingRoomEntity.getServicePoints())
      .ifPresent(spEntities -> spEntities.forEach(spEntity -> spEntity.setReadingRoom(readingRoomEntity)));
  }
}
