package com.siec_acc.repository;

import com.siec_acc.entity.GoodsReceiptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceiptEntity, Long> {
    Optional<GoodsReceiptEntity> findByGrnStrId(String grnStrId);

    // Used by PurchaseOrderService (block delete / guard item edits) and PurchaseBillService (bill needs goods received).
    boolean existsByPoStrId(String poStrId);
    boolean existsByPoNumberIgnoreCase(String poNumber);

    // Oldest first: the running total per item decides each GRN's partial/completed status.
    List<GoodsReceiptEntity> findByPoStrIdOrderByGrnPrimeIdAsc(String poStrId);

    List<GoodsReceiptEntity> findAllByOrderByCreatedAtDesc();
}

