package org.folio.readingroom.service.impl;


import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.domain.entity.ReadingRoomServicePointEntity;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.exception.ServicePointException;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.repository.ReadingRoomServicePointRepository;
import org.folio.readingroom.service.InventoryServicePointService;
import org.folio.readingroom.service.ReadingRoomService;
import org.folio.readingroom.service.converter.ReadingRoomMapper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Log4j2
public class ReadingRoomServiceImpl implements ReadingRoomService {

  private final ReadingRoomRepository readingRoomRepository;
  private final ReadingRoomMapper readingRoomMapper;
  private final ReadingRoomServicePointRepository rrServicePointRepository;
  private final InventoryServicePointService inventoryServicePointService;

  @Override
  public ReadingRoom createReadingRoom(ReadingRoom readingRoomDto) {
    log.debug("createReadingRoom:: creating reading room with {}", readingRoomDto);
    validateReadingRoom(readingRoomDto.getId());
    validateServicePoints(readingRoomDto.getServicePoints());
    ReadingRoomEntity readingRoomEntity = readingRoomRepository.save(readingRoomMapper.toEntity(readingRoomDto));
    return readingRoomMapper.toDto(readingRoomEntity);
  }

  private void validateReadingRoom(UUID readingRoomId) {
    log.debug("validateReadingRoom:: validating readingRoom with id {}", readingRoomId);
    readingRoomRepository.findById(readingRoomId)
      .ifPresent(entity -> {
        throw new ResourceAlreadyExistException("Reading room with id " + readingRoomId + " already exists");
      });
  }

  private void validateServicePoints(List<ServicePoint> servicePointDtoList) {
    log.debug("validateServicePoints:: validating servicePoints with {}", servicePointDtoList);
    var servicePointIds = servicePointDtoList
      .stream()
      .map(ServicePoint::getId)
      .toList();
    var invalidServicePointList = inventoryServicePointService.fetchInvalidServicePointList(servicePointIds);
    if (!invalidServicePointList.isEmpty()) {
      throw new ServicePointException("ServicePointId %s doesn't exists in inventory", invalidServicePointList);
    }
    var existingServicePointList = rrServicePointRepository.findAllById(servicePointIds);
    if (!existingServicePointList.isEmpty()) {
      throw new ServicePointException("ServicePointId %s already associated with another Reading room",
        existingServicePointList
          .stream()
          .map(ReadingRoomServicePointEntity::getServicePointId)
          .toList());
    }
  }

}
