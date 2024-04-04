package org.folio.readingroom.service;

import java.util.List;
import java.util.UUID;

public interface ServicePointService {

  /**
   * Fetches a list of invalid service point IDs.
   *
   * @param servicePointIds The list of service point IDs to be checked for validity.
   * @return A list of invalid service point IDs. If all provided service point IDs are valid,an empty list is returned.
   */
  List<UUID> fetchInvalidServicePointList(List<UUID> servicePointIds);

}
