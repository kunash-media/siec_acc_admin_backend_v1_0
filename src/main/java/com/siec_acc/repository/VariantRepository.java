package com.siec_acc.repository;

import com.siec_acc.dto.response.VariantLiteResponseDTO;
import com.siec_acc.entity.VariantEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface VariantRepository extends JpaRepository<VariantEntity, Long> {
    Optional<VariantEntity> findByVariantStrId(String variantStrId);
    List<VariantEntity> findByProduct_ProductStrId(String productStrId);
    boolean existsByVariantSkuIgnoreCase(String variantSku);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from VariantEntity v where v.variantStrId = :variantStrId")
    Optional<VariantEntity> findByVariantStrIdForUpdate(@Param("variantStrId") String variantStrId);

    @Query("select new com.siec_acc.dto.response.VariantLiteResponseDTO("
            + "v.variantPrimeId, v.variantStrId, v.product.productStrId, v.variantName, v.variantMrpPrice, variantSellingPrice, v.variantSku, v.variantUnit, v.variantSize) "
            + "from VariantEntity v where v.product.productStrId in :productStrIds "
            + "order by v.variantName asc, v.variantPrimeId asc")
    List<VariantLiteResponseDTO> findLiteByProductStrIds(@Param("productStrIds") Collection<String> productStrIds);
}