package org.folio.readingroom.service;

import java.util.List;
import java.util.UUID;

public interface InventoryServicePointService {
  List<UUID> fetchInvalidServicePointList(List<UUID> servicePointIds);

}
