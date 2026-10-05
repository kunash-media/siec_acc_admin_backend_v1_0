package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.WarehouseRequestDTO;
import com.siec_acc.dto.response.WarehouseResponseDTO;
import com.siec_acc.entity.WarehouseEntity;
import com.siec_acc.exceptions.DuplicateResourceException;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.WarehouseRepository;
import com.siec_acc.service.WarehouseService;
import com.siec_acc.utils.StrIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseServiceImpl implements WarehouseService {

    private static final Logger logger = LoggerFactory.getLogger(WarehouseServiceImpl.class);
    private final WarehouseRepository warehouseRepository;

    public WarehouseServiceImpl(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Override
    @Transactional
    public WarehouseResponseDTO createWarehouse(WarehouseRequestDTO requestDTO) {
        String name = cleanName(requestDTO.getWarehouseName());
        if (warehouseRepository.findByWarehouseNameIgnoreCase(name).isPresent()) {
            throw new DuplicateResourceException("A warehouse with name '" + name + "' already exists.");
        }
        boolean first = warehouseRepository.count() == 0;
        WarehouseEntity saved = warehouseRepository.save(WarehouseEntity.builder()
                .warehouseName(name)
                .warehouseAddress(requestDTO.getWarehouseAddress())
                .warehouseIsDefault(first)
                .warehouseStatus("ACTIVE")
                .build());
        saved.setWarehouseStrId(StrIdGenerator.generate("WH", saved.getWarehousePrimeId()));
        saved = warehouseRepository.save(saved);
        logger.info("Warehouse created: {}", saved.getWarehouseStrId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public WarehouseResponseDTO updateWarehouse(String warehouseStrId, WarehouseRequestDTO requestDTO) {
        WarehouseEntity warehouse = getOrThrow(warehouseStrId);
        String name = cleanName(requestDTO.getWarehouseName());
        warehouseRepository.findByWarehouseNameIgnoreCase(name).ifPresent(other -> {
            if (!other.getWarehousePrimeId().equals(warehouse.getWarehousePrimeId())) {
                throw new DuplicateResourceException("A warehouse with name '" + name + "' already exists.");
            }
        });
        warehouse.setWarehouseName(name);
        warehouse.setWarehouseAddress(requestDTO.getWarehouseAddress());
        return mapToResponse(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public WarehouseResponseDTO setDefaultWarehouse(String warehouseStrId) {
        WarehouseEntity target = getOrThrow(warehouseStrId);
        if (!"ACTIVE".equals(target.getWarehouseStatus())) {
            throw new IllegalArgumentException("An inactive warehouse cannot be the default.");
        }
        if (Boolean.TRUE.equals(target.getWarehouseIsDefault())) return mapToResponse(target);
        warehouseRepository.clearDefault();
        WarehouseEntity fresh = getOrThrow(warehouseStrId);
        fresh.setWarehouseIsDefault(true);
        logger.info("Default warehouse set to {}", warehouseStrId);
        return mapToResponse(warehouseRepository.save(fresh));
    }

    @Override
    @Transactional
    public WarehouseResponseDTO changeStatus(String warehouseStrId, boolean active) {
        WarehouseEntity warehouse = getOrThrow(warehouseStrId);
        if (!active && Boolean.TRUE.equals(warehouse.getWarehouseIsDefault())) {
            throw new IllegalArgumentException("Default warehouse cannot be deactivated. Set another warehouse as default first.");
        }
        warehouse.setWarehouseStatus(active ? "ACTIVE" : "INACTIVE");
        return mapToResponse(warehouseRepository.save(warehouse));
    }

    @Override
    public WarehouseResponseDTO getWarehouseByStrId(String warehouseStrId) {
        return mapToResponse(getOrThrow(warehouseStrId));
    }

    @Override
    public List<WarehouseResponseDTO> getAllWarehouses() {
        return warehouseRepository.findAll().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public WarehouseEntity resolveWarehouse(String warehouseStrId) {
        if (warehouseStrId == null || warehouseStrId.isBlank()) {
            return warehouseRepository.findByWarehouseIsDefaultTrue()
                    .orElseThrow(() -> new ResourceNotFoundException("No default warehouse is configured."));
        }
        return getOrThrow(warehouseStrId.trim());
    }

    @Override
    public WarehouseEntity resolveActiveWarehouse(String warehouseStrId) {
        WarehouseEntity warehouse = resolveWarehouse(warehouseStrId);
        if (!"ACTIVE".equals(warehouse.getWarehouseStatus())) {
            throw new IllegalArgumentException("Warehouse '" + warehouse.getWarehouseStrId() + "' is inactive and cannot receive stock.");
        }
        return warehouse;
    }

    // ---------- helpers ----------

    private WarehouseEntity getOrThrow(String warehouseStrId) {
        return warehouseRepository.findByWarehouseStrId(warehouseStrId)
                .orElseThrow(() -> new ResourceNotFoundException("No warehouse found with ID '" + warehouseStrId + "'."));
    }

    private String cleanName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Warehouse name is required.");
        return name.trim();
    }

    private WarehouseResponseDTO mapToResponse(WarehouseEntity w) {
        return WarehouseResponseDTO.builder()
                .warehousePrimeId(w.getWarehousePrimeId())
                .warehouseStrId(w.getWarehouseStrId())
                .warehouseName(w.getWarehouseName())
                .warehouseAddress(w.getWarehouseAddress())
                .warehouseIsDefault(w.getWarehouseIsDefault())
                .warehouseStatus(w.getWarehouseStatus())
                .warehouseCreatedAt(w.getWarehouseCreatedAt())
                .warehouseUpdatedAt(w.getWarehouseUpdatedAt())
                .build();
    }
}