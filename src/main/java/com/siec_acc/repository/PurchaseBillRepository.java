package com.siec_acc.repository;
import com.siec_acc.entity.PurchaseBillEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PurchaseBillRepository extends JpaRepository<PurchaseBillEntity, Long> {
    Optional<PurchaseBillEntity> findByPbStrId(String pbStrId);
    Optional<PurchaseBillEntity> findByPbNumber(String pbNumber);

    boolean existsByPoNumberIgnoreCase(String poNumber);

    List<PurchaseBillEntity> findAllByOrderByCreatedAtDesc();
    List<PurchaseBillEntity> findByVendorNameContainingIgnoreCaseOrderByCreatedAtDesc(String vendorName);
    List<PurchaseBillEntity> findByPoNumberIgnoreCaseOrderByCreatedAtDesc(String poNumber);
}

