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
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  unmappedTargetPolicy = ReportingPolicy.IGNORE,
  builder = @Builder(disableBuilder = true))
public interface ReadingRoomMapper {

  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "createdDate", ignore = true)
  @Mapping(target = "updatedBy", ignore = true)
  @Mapping(target = "updatedDate", ignore = true)
  ReadingRoomEntity toEntity(ReadingRoom readingRoom);

  ReadingRoomServicePointEntity toEntity(ServicePoint servicePoint);

  ServicePoint toDto(ReadingRoomServicePointEntity servicePointEntity);

  @Mapping(target = "metadata.createdByUserId", source = "createdBy")
  @Mapping(target = "metadata.createdDate", source = "createdDate")
  @Mapping(target = "metadata.updatedByUserId", source = "updatedBy")
  @Mapping(target = "metadata.updatedDate", source = "updatedDate")
  org.folio.readingroom.domain.dto.ReadingRoom toDto(ReadingRoomEntity readingRoomEntity);

  @AfterMapping
  default void setReadingRoom(@MappingTarget ReadingRoomEntity readingRoomEntity) {
    Optional.ofNullable(readingRoomEntity.getServicePoints())
      .ifPresent(spEntities -> spEntities.forEach(spEntity -> spEntity.setReadingRoom(readingRoomEntity)));
  }
}
