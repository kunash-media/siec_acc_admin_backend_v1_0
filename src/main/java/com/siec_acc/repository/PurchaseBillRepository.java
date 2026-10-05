package com.siec_acc.repository;
import com.siec_acc.entity.PurchaseBillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseBillRepository extends JpaRepository<PurchaseBillEntity, Long> {
    Optional<PurchaseBillEntity> findByPbStrId(String pbStrId);
    Optional<PurchaseBillEntity> findByPbNumber(String pbNumber);

    // Used by PurchaseOrderService to block deleting a PO that already has bills against it.
    boolean existsByPoNumberIgnoreCase(String poNumber);

    // Newest first, same order the frontend shows
    List<PurchaseBillEntity> findAllByOrderByCreatedAtDesc();
    List<PurchaseBillEntity> findByVendorNameContainingIgnoreCaseOrderByCreatedAtDesc(String vendorName);
    List<PurchaseBillEntity> findByPoNumberIgnoreCaseOrderByCreatedAtDesc(String poNumber);
}

