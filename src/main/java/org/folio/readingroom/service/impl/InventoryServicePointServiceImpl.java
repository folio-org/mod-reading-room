package org.folio.readingroom.service.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.readingroom.client.feign.InventoryServicePointClient;
import org.folio.readingroom.service.InventoryServicePointService;
import org.folio.readingroom.utils.CqlHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class InventoryServicePointServiceImpl implements InventoryServicePointService {

  private final InventoryServicePointClient inventoryServicePointClient;

  @Override
  public List<UUID> fetchInvalidServicePointList(List<UUID> servicePointIds) {
    log.debug("fetchInvalidServicePointList:: fetching invalid servicePointIds {}", servicePointIds);
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
