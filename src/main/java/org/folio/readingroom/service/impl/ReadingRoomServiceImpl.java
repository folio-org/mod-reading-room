package org.folio.readingroom.service.impl;

import static java.util.Comparator.comparing;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.AccessLog;
import org.folio.readingroom.domain.dto.AccessLogCollection;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ReadingRoomCollection;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.folio.readingroom.exception.IdMismatchException;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.exception.ServicePointException;
import org.folio.readingroom.repository.AccessLogRepository;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.repository.ReadingRoomServicePointRepository;
import org.folio.readingroom.service.ReadingRoomService;
import org.folio.readingroom.service.ServicePointService;
import org.folio.readingroom.service.converter.Mapper;
import org.folio.spring.data.OffsetRequest;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class ReadingRoomServiceImpl implements ReadingRoomService {

  private final ReadingRoomRepository readingRoomRepository;
  private final Mapper mapper;
  private final ReadingRoomServicePointRepository rrServicePointRepository;
  private final ServicePointService servicePointService;
  private final AccessLogRepository accessLogRepository;

  @Override
  public ReadingRoom createReadingRoom(ReadingRoom readingRoomDto) {
    log.debug("createReadingRoom:: creating reading room with {}", readingRoomDto);
    checkReadingRoomExistsAndThrow(readingRoomDto.getId());
    validateServicePoints(readingRoomDto.getServicePoints());
    ReadingRoomEntity readingRoomEntity = readingRoomRepository.save(mapper.toEntity(readingRoomDto));
    return mapper.toDto(readingRoomEntity);
  }

  @Override
  public ReadingRoom updateReadingRoom(UUID readingRoomId, ReadingRoom readingRoomDto) {
    log.debug("updateReadingRoom:: updating reading room with {}", readingRoomDto);
    if (!readingRoomId.equals(readingRoomDto.getId())) {
      throw new IdMismatchException(
        "The ID provided in the request URL does not match the ID of the resource in the request body");
    }
    var existingEntity = getReadingRoomByIdOrThrow(readingRoomId);
    validateServicePoints(readingRoomDto.getServicePoints(), readingRoomId);
    updateModifiedFields(existingEntity, mapper.toEntity(readingRoomDto));
    return mapper.toDto(readingRoomRepository.save(existingEntity));
  }

  @Override
  public void deleteReadingRoomById(UUID readingRoomId) {
    log.debug("deleteReadingRoomById:: soft deleting reading room with id {}", readingRoomId);
    ReadingRoomEntity readingRoomEntity = getReadingRoomByIdOrThrow(readingRoomId);
    readingRoomEntity.setDeleted(true);
    readingRoomEntity.getServicePoints().clear();
    readingRoomRepository.save(readingRoomEntity);
  }

  private ReadingRoomEntity getReadingRoomByIdOrThrow(UUID readingRoomId) {
    return readingRoomRepository.findById(readingRoomId)
      .orElseThrow(() ->
        new NotFoundException(String.format("Reading room with id %s doesn't exists", readingRoomId)));
  }

  @Override
  public ReadingRoomCollection getReadingRoomsByCqlQuery(String query, Integer offset, Integer limit,
                                                         Boolean includeDeleted) {
    log.debug("getReadingRoomsByCqlQuery:: fetch reading room list by cql query {}, offset {}, "
      + "limit {}, includeDeleted {}", query, offset, limit, includeDeleted);
    var readingRooms = readingRoomRepository.findByCql(query, OffsetRequest.of(offset, limit));
    return mapper.toDto(readingRooms, includeDeleted);
  }

  @Override
  public AccessLog createAccessLog(UUID readingRoomId, AccessLog accessLog) {
    log.debug("createAccessLog:: create access log with {}", accessLog);
    if (!readingRoomId.equals(accessLog.getReadingRoomId())) {
      throw new IdMismatchException(
        "The reading room ID provided in the request URL does not match the ID of the resource in the request body");
    }
    checkAccessLogExistsAndThrow(accessLog.getId());
    var accessLogEntity = accessLogRepository.save(mapper.toEntity(accessLog));
    return mapper.toDto(accessLogEntity);
  }

  @Override
  public AccessLogCollection getAccessLogsByCqlQuery(String query, Integer offset, Integer limit) {
    log.debug("getAccessLogsByCqlQuery:: fetch accessLog entries with cql query{}, offset {}, "
      + "limit {}", query, offset, limit);
    var accessLogs = accessLogRepository.findByCql(query, OffsetRequest.of(offset, limit));
    return mapper.toDtoCollection(accessLogs);
  }

  private void checkReadingRoomExistsAndThrow(UUID readingRoomId) {
    log.debug("checkReadingRoomExistsAndThrow:: checking the readingRoom with id {} exists already", readingRoomId);
    readingRoomRepository.findById(readingRoomId)
      .ifPresent(entity -> {
        throw new ResourceAlreadyExistException(String.format("Reading room with id %s already exists", readingRoomId));
      });
  }

  private void checkAccessLogExistsAndThrow(UUID accessLogId) {
    log.debug("checkAccessLogExistsAndThrow:: checking the access log with id {} exists already", accessLogId);
    accessLogRepository.findById(accessLogId)
      .ifPresent(entity -> {
        throw new ResourceAlreadyExistException(String.format("Access log with id %s already exists", accessLogId));
      });
  }

  private void updateModifiedFields(ReadingRoomEntity existingEntity, ReadingRoomEntity newEntity) {
    log.debug("updateModifiedFields:: updating existing entity {} with new entity {}", existingEntity, newEntity);
    existingEntity.setName(newEntity.getName());
    existingEntity.setIsPublic(newEntity.getIsPublic());
    //set the last updated field so that metadata field gets updated even if child entity is changed
    existingEntity.setUpdatedDate(LocalDateTime.now());
    var existingServicePoints = new ArrayList<>(emptyIfNull(existingEntity.getServicePoints()));
    var updatedServicePoints = new ArrayList<>(emptyIfNull(newEntity.getServicePoints()));
    // Update the existing entity of readingRoomServicePoint based on the incoming request
    existingServicePoints
      .forEach(entity -> {
        int idx = Collections.binarySearch(updatedServicePoints, entity,
          comparing(ReadingRoomServicePointEntity::getId));
        if (idx >= 0) {
          entity.setName(updatedServicePoints.get(idx).getName());
          updatedServicePoints.remove(idx);
        } else {
          existingEntity.removeServicePoints(entity);
        }
      });
    updatedServicePoints.forEach(existingEntity::addServicePoints);
  }


  private void validateServicePoints(Set<ServicePoint> servicePointDtoList) {
    log.debug("validateServicePoints:: validating servicePoints with {}", servicePointDtoList);
    var servicePointIds = servicePointDtoList
      .stream()
      .map(ServicePoint::getValue)
      .toList();
    checkInvalidServicePoints(servicePointIds);
    var existingServicePointList = rrServicePointRepository.findAllById(servicePointIds);
    if (!existingServicePointList.isEmpty()) {
      throw new ServicePointException("ServicePointId %s already associated with another Reading room",
        getServicePointIdsFromEntities(existingServicePointList));
    }
  }

  private void validateServicePoints(Set<ServicePoint> servicePointDtoList, UUID readingRoomId) {
    log.debug("validateServicePoints:: validating servicePoints with {}", servicePointDtoList);
    var servicePointIds = servicePointDtoList
      .stream()
      .map(ServicePoint::getValue)
      .toList();
    checkInvalidServicePoints(servicePointIds);
    // check if the service point ids associated with other reading rooms already
    var existingServicePointList = rrServicePointRepository
      .findAllByIdInAndReadingRoomIdNot(servicePointIds, readingRoomId);
    if (!existingServicePointList.isEmpty()) {
      throw new ServicePointException("ServicePointId %s already associated with another Reading room",
        getServicePointIdsFromEntities(existingServicePointList));
    }
  }

  private void checkInvalidServicePoints(List<UUID> servicePointIds) {
    var invalidServicePointList = servicePointService.fetchInvalidServicePointList(servicePointIds);
    if (!invalidServicePointList.isEmpty()) {
      throw new ServicePointException("ServicePointId %s doesn't exists in inventory", invalidServicePointList);
    }
  }

  private List<UUID> getServicePointIdsFromEntities(List<ReadingRoomServicePointEntity> servicePointEntities) {
    return servicePointEntities
      .stream()
      .map(ReadingRoomServicePointEntity::getId)
      .toList();
  }

}
