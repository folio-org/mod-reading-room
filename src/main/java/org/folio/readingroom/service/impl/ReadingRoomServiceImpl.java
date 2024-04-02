package org.folio.readingroom.service.impl;


import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.folio.readingroom.domain.dto.ReadingRoom;
import org.folio.readingroom.domain.dto.ServicePoint;
import org.folio.readingroom.domain.entity.ReadingRoomEntity;
import org.folio.readingroom.exception.BadRequestException;
import org.folio.readingroom.exception.ResourceAlreadyExistException;
import org.folio.readingroom.repository.ReadingRoomRepository;
import org.folio.readingroom.repository.ReadingRoomServicePointRepository;
import org.folio.readingroom.service.InventoryServicePointService;
import org.folio.readingroom.service.ReadingRoomService;
import org.folio.readingroom.service.converter.ReadingRoomMapper;
import org.folio.spring.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReadingRoomServiceImpl implements ReadingRoomService {

  private final ReadingRoomRepository readingRoomRepository;
  private final ReadingRoomMapper readingRoomMapper;
  private final ReadingRoomServicePointRepository rrServicePointRepository;
  private final InventoryServicePointService inventoryServicePointService;

  @Override
  public ReadingRoom createReadingRoom(ReadingRoom readingRoomDto) {
    validateReadingRoom(readingRoomDto.getId());
    validateServicePoints(readingRoomDto.getServicePoints());
    ReadingRoomEntity readingRoomEntity = readingRoomRepository.save(readingRoomMapper.toEntity(readingRoomDto));
    return readingRoomMapper.toDto(readingRoomEntity);
  }

  private void validateReadingRoom(UUID readingRoomId) {
    readingRoomRepository.findById(readingRoomId)
      .ifPresent(entity -> {
        throw new ResourceAlreadyExistException("Reading room with id " + readingRoomId + " already exists");
      });
  }

  private void validateServicePoints(List<ServicePoint> servicePointDtoList) {
    List<UUID> servicePointIds = servicePointDtoList
      .stream()
      .map(ServicePoint::getId)
      .toList();
    if (!inventoryServicePointService.fetchInvalidServicePointList(servicePointIds).isEmpty()) {
      throw new NotFoundException("One of the ServicePointId doesn't exist");
    }
    if (!rrServicePointRepository.findAllById(servicePointIds).isEmpty()) {
      throw new BadRequestException("One of the servicePointId already associated with existing Reading room");
    }
  }

}
