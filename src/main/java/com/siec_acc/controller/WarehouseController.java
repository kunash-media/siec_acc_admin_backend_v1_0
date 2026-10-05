package com.siec_acc.controller;

import com.siec_acc.dto.request.WarehouseRequestDTO;
import com.siec_acc.dto.response.WarehouseResponseDTO;
import com.siec_acc.exceptions.ApiResponse;
import com.siec_acc.service.WarehouseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses/v1")
public class WarehouseController {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseController.class);
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @PostMapping("/create-warehouse")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> create(@RequestBody WarehouseRequestDTO requestDTO) {
        logger.info("API HIT: POST /create-warehouse | name={}", requestDTO.getWarehouseName());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Warehouse created successfully.", warehouseService.createWarehouse(requestDTO)));
    }

    @PutMapping("/update-warehouse/{warehouseStrId}")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> update(@PathVariable String warehouseStrId, @RequestBody WarehouseRequestDTO requestDTO) {
        logger.info("API HIT: PUT /update-warehouse/{}", warehouseStrId);
        return ResponseEntity.ok(ApiResponse.success("Warehouse updated successfully.", warehouseService.updateWarehouse(warehouseStrId, requestDTO)));
    }

    @PatchMapping("/set-default/{warehouseStrId}")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> setDefault(@PathVariable String warehouseStrId) {
        logger.info("API HIT: PATCH /set-default/{}", warehouseStrId);
        return ResponseEntity.ok(ApiResponse.success("Default warehouse updated successfully.", warehouseService.setDefaultWarehouse(warehouseStrId)));
    }

    @PatchMapping("/activate/{warehouseStrId}")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> activate(@PathVariable String warehouseStrId) {
        logger.info("API HIT: PATCH /activate/{}", warehouseStrId);
        return ResponseEntity.ok(ApiResponse.success("Warehouse activated successfully.", warehouseService.changeStatus(warehouseStrId, true)));
    }

    @PatchMapping("/deactivate/{warehouseStrId}")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> deactivate(@PathVariable String warehouseStrId) {
        logger.info("API HIT: PATCH /deactivate/{}", warehouseStrId);
        return ResponseEntity.ok(ApiResponse.success("Warehouse deactivated successfully.", warehouseService.changeStatus(warehouseStrId, false)));
    }

    @GetMapping("/get-warehouse/{warehouseStrId}")
    public ResponseEntity<ApiResponse<WarehouseResponseDTO>> get(@PathVariable String warehouseStrId) {
        logger.info("API HIT: GET /get-warehouse/{}", warehouseStrId);
        return ResponseEntity.ok(ApiResponse.success("Warehouse fetched successfully.", warehouseService.getWarehouseByStrId(warehouseStrId)));
    }

    @GetMapping("/get-all-warehouses")
    public ResponseEntity<ApiResponse<List<WarehouseResponseDTO>>> getAll() {
        logger.info("API HIT: GET /get-all-warehouses");
        return ResponseEntity.ok(ApiResponse.success("Warehouses fetched successfully.", warehouseService.getAllWarehouses()));
    }
}