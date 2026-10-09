package com.siec_acc.repository;

import com.siec_acc.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {
    Optional<PurchaseOrderEntity> findByPoStrId(String poStrId);
    Optional<PurchaseOrderEntity> findByPoNumber(String poNumber);
    boolean existsByPoNumberIgnoreCase(String poNumber);

    // Newest first, same order the frontend shows
    List<PurchaseOrderEntity> findAllByOrderByCreatedAtDesc();
    List<PurchaseOrderEntity> findByStatusIgnoreCaseOrderByCreatedAtDesc(String status);
    List<PurchaseOrderEntity> findByVendorNameContainingIgnoreCaseOrderByCreatedAtDesc(String vendorName);
}
