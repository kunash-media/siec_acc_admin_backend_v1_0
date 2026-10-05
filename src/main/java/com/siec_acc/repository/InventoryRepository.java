package com.siec_acc.repository;

import com.siec_acc.entity.InventoryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long> {
    List<InventoryEntity> findAllByProduct_ProductPrimeId(Long productPrimeId);
    List<InventoryEntity> findAllByProduct_ProductStrId(String productStrId);
    List<InventoryEntity> findAllByVariant_VariantPrimeId(Long variantPrimeId);
    List<InventoryEntity> findAllByVariant_VariantStrId(String variantStrId);

    Optional<InventoryEntity> findByProduct_ProductStrIdAndWarehouse_WarehouseStrId(String productStrId, String warehouseStrId);
    Optional<InventoryEntity> findByVariant_VariantStrIdAndWarehouse_WarehouseStrId(String variantStrId, String warehouseStrId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryEntity i where i.product.productStrId = :productStrId and i.warehouse.warehouseStrId = :warehouseStrId")
    Optional<InventoryEntity> findProductRowForUpdate(@Param("productStrId") String productStrId, @Param("warehouseStrId") String warehouseStrId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InventoryEntity i where i.variant.variantStrId = :variantStrId and i.warehouse.warehouseStrId = :warehouseStrId")
    Optional<InventoryEntity> findVariantRowForUpdate(@Param("variantStrId") String variantStrId, @Param("warehouseStrId") String warehouseStrId);

    @Query("select sum(i.productStock) from InventoryEntity i where i.product.productPrimeId = :productPrimeId")
    BigDecimal sumStockByProductPrimeId(@Param("productPrimeId") Long productPrimeId);

    @Query("select sum(i.productStock) from InventoryEntity i where i.variant.variantPrimeId = :variantPrimeId")
    BigDecimal sumStockByVariantPrimeId(@Param("variantPrimeId") Long variantPrimeId);
}