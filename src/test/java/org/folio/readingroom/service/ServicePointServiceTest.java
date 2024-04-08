package org.folio.readingroom.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.folio.readingroom.client.feign.ServicePointClient;
import org.folio.readingroom.service.impl.ServicePointServiceImpl;
import org.folio.spring.model.ResultList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServicePointServiceTest {
  @Mock
  ServicePointClient servicePointClient;
  @InjectMocks
  ServicePointServiceImpl servicePointServiceImpl;

  List<UUID> servicePointIds;
  List<ServicePointClient.InventoryServicePoint> inventoryServicePoints;
  ResultList<ServicePointClient.InventoryServicePoint> resultList;

  @BeforeEach
  void initData() {
    servicePointIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
  }

  @Test
  void fetchInvalidServicePointList_WhenAllPointsAreValid() {
    inventoryServicePoints = Arrays.asList(
      new ServicePointClient.InventoryServicePoint(servicePointIds.get(0).toString(), "Circ-desk1"),
      new ServicePointClient.InventoryServicePoint(servicePointIds.get(1).toString(), "Online")
    );
    resultList = new ResultList<>();
    resultList.setResult(inventoryServicePoints);
    when(servicePointClient.getServicePointsByIds(any())).thenReturn(resultList);
    List<UUID> invalidServicePointsList = servicePointServiceImpl.fetchInvalidServicePointList(servicePointIds);
    verify(servicePointClient).getServicePointsByIds(any());
    assertEquals(0, invalidServicePointsList.size());
  }

  @Test
  void fetchInvalidServicePointList_WhenSomePointsAreInvalid() {
    inventoryServicePoints = List.of(
      new ServicePointClient.InventoryServicePoint(servicePointIds.get(0).toString(), "Circ-desk1")
    );
    resultList = new ResultList<>();
    resultList.setResult(inventoryServicePoints);
    when(servicePointClient.getServicePointsByIds(any())).thenReturn(resultList);
    List<UUID> invalidServicePointsList = servicePointServiceImpl.fetchInvalidServicePointList(servicePointIds);
    verify(servicePointClient).getServicePointsByIds(any());
    assertEquals(1, invalidServicePointsList.size());
  }
}

