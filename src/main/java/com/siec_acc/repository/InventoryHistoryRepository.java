package com.siec_acc.repository;

import com.siec_acc.entity.InventoryHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistoryEntity, Long> {
    List<InventoryHistoryEntity> findByProductStrIdOrderByHistoryCreatedAtDesc(String productStrId);
    List<InventoryHistoryEntity> findByVariantStrIdOrderByHistoryCreatedAtDesc(String variantStrId);
}