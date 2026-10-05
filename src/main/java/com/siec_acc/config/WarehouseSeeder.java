package com.siec_acc.config;

import com.siec_acc.dto.request.WarehouseRequestDTO;
import com.siec_acc.repository.WarehouseRepository;
import com.siec_acc.service.WarehouseService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class WarehouseSeeder implements ApplicationRunner {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseService warehouseService;

    public WarehouseSeeder(WarehouseRepository warehouseRepository, WarehouseService warehouseService) {
        this.warehouseRepository = warehouseRepository;
        this.warehouseService = warehouseService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (warehouseRepository.count() > 0) return;
        WarehouseRequestDTO dto = new WarehouseRequestDTO();
        dto.setWarehouseName("Main Warehouse");
        warehouseService.createWarehouse(dto); // first warehouse becomes the default automatically
    }
}