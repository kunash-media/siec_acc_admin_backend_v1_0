package com.siec_acc.repository;

import com.siec_acc.entity.VendorPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface VendorPaymentRepository extends JpaRepository<VendorPaymentEntity, Long> {

    Optional<VendorPaymentEntity> findByVpStrId(String vpStrId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from VendorPaymentEntity p where p.vpStrId = :vpStrId")
    Optional<VendorPaymentEntity> findByVpStrIdForUpdate(@Param("vpStrId") String vpStrId);

    List<VendorPaymentEntity> findAllByOrderByDateDescVpPrimeIdDesc();
}