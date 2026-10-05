package com.siec_acc.repository;


import com.siec_acc.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByProductStrId(String productStrId);
    boolean existsByProductSkuIgnoreCase(String productSku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductEntity p where p.productStrId = :productStrId")
    Optional<ProductEntity> findByProductStrIdForUpdate(@Param("productStrId") String productStrId);
}
