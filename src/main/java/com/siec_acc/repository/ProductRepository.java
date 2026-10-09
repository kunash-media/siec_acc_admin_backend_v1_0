package com.siec_acc.repository;


import com.siec_acc.dto.response.ProductLiteResponseDTO;
import com.siec_acc.entity.ProductEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByProductStrId(String productStrId);
    boolean existsByProductSkuIgnoreCase(String productSku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductEntity p where p.productStrId = :productStrId")
    Optional<ProductEntity> findByProductStrIdForUpdate(@Param("productStrId") String productStrId);

    @Query("select new com.siec_acc.dto.response.ProductLiteResponseDTO("
            + "p.productPrimeId, p.productStrId, p.productName, p.productUnit, p.productSize, "
            + "p.productSellingPrice, p.productMrpPrice, p.productVendorName) "
            + "from ProductEntity p order by p.productName asc, p.productPrimeId asc")
    Slice<ProductLiteResponseDTO> findAllLite(Pageable pageable);

    @Query("select new com.siec_acc.dto.response.ProductLiteResponseDTO("
            + "p.productPrimeId, p.productStrId, p.productName, p.productUnit, p.productSize, "
            + "p.productSellingPrice, p.productMrpPrice, p.productVendorName) "
            + "from ProductEntity p where "
            + "lower(p.productName) like :search escape '!' "
            + "or lower(p.productSku) like :search escape '!' "
            + "or lower(p.productStrId) like :search escape '!' "
            + "or exists (select 1 from VariantEntity v where v.product = p and ("
            + "lower(v.variantName) like :search escape '!' "
            + "or lower(v.variantSku) like :search escape '!' "
            + "or lower(v.variantStrId) like :search escape '!')) "
            + "order by p.productName asc, p.productPrimeId asc")
    Slice<ProductLiteResponseDTO> searchLite(@Param("search") String search, Pageable pageable);
}
