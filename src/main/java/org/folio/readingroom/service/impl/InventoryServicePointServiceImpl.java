package org.folio.readingroom.service.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.folio.readingroom.client.feign.InventoryServicePointClient;
import org.folio.readingroom.service.InventoryServicePointService;
import org.folio.readingroom.util.CqlHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryServicePointServiceImpl implements InventoryServicePointService {

  private final InventoryServicePointClient inventoryServicePointClient;

  @Override
  public List<UUID> fetchInvalidServicePointList(List<UUID> servicePointIds) {
    List<UUID> inventoryServicePointIds = inventoryServicePointClient
      .getServicePointByName(CqlHelper.getAnyMatch(servicePointIds))
      .getResult()
      .stream()
      .map(inventoryServicePoint -> UUID.fromString(inventoryServicePoint.id()))
      .toList();
    return servicePointIds
      .stream()
      .filter(ids -> !inventoryServicePointIds.contains(ids))
      .toList();
  }

}
