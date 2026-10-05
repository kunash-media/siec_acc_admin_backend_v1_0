package com.siec_acc.repository;

import com.siec_acc.entity.WarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<WarehouseEntity, Long> {

    Optional<WarehouseEntity> findByWarehouseStrId(String warehouseStrId);

    Optional<WarehouseEntity> findByWarehouseIsDefaultTrue();

    Optional<WarehouseEntity> findByWarehouseNameIgnoreCase(String warehouseName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update WarehouseEntity w set w.warehouseIsDefault = false where w.warehouseIsDefault = true")
    void clearDefault();
}
