package com.siec_acc.service;

import com.siec_acc.dto.request.WarehouseRequestDTO;
import com.siec_acc.dto.response.WarehouseResponseDTO;
import com.siec_acc.entity.WarehouseEntity;

import java.util.List;

public interface WarehouseService {
    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO requestDTO);
    WarehouseResponseDTO updateWarehouse(String warehouseStrId, WarehouseRequestDTO requestDTO);
    WarehouseResponseDTO setDefaultWarehouse(String warehouseStrId);
    WarehouseResponseDTO changeStatus(String warehouseStrId, boolean active);
    WarehouseResponseDTO getWarehouseByStrId(String warehouseStrId);
    List<WarehouseResponseDTO> getAllWarehouses();
    WarehouseEntity resolveWarehouse(String warehouseStrId);        // blank -> default, any status
    WarehouseEntity resolveActiveWarehouse(String warehouseStrId);  // blank -> default, must be ACTIVE (stock-in)
}