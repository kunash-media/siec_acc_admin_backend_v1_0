package com.siec_acc.repository;
import com.siec_acc.entity.PurchaseBillEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseBillRepository extends JpaRepository<PurchaseBillEntity, Long> {
    Optional<PurchaseBillEntity> findByPbStrId(String pbStrId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from PurchaseBillEntity b where b.pbStrId = :pbStrId")
    Optional<PurchaseBillEntity> findByPbStrIdForUpdate(@Param("pbStrId") String pbStrId);
    Optional<PurchaseBillEntity> findByPbNumber(String pbNumber);

    boolean existsByPoNumberIgnoreCase(String poNumber);

    List<PurchaseBillEntity> findAllByOrderByCreatedAtDesc();
    List<PurchaseBillEntity> findByVendorNameContainingIgnoreCaseOrderByCreatedAtDesc(String vendorName);

    // Exact (case-insensitive) vendor match - unlike the "Containing" version above, "ABC" does not return "ABC Traders".
    List<PurchaseBillEntity> findByVendorNameIgnoreCaseOrderByCreatedAtDesc(String vendorName);
    List<PurchaseBillEntity> findByPoNumberIgnoreCaseOrderByCreatedAtDesc(String poNumber);
}

