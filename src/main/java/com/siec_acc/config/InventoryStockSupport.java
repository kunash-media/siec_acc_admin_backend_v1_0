package com.siec_acc.config;

import com.siec_acc.entity.*;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.InventoryHistoryRepository;
import com.siec_acc.repository.InventoryRepository;
import com.siec_acc.repository.ProductRepository;
import com.siec_acc.repository.VariantRepository;
import com.siec_acc.utils.StrIdGenerator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InventoryStockSupport {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    public InventoryStockSupport(InventoryRepository inventoryRepository, InventoryHistoryRepository inventoryHistoryRepository,
                                 ProductRepository productRepository, VariantRepository variantRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    public InventoryEntity createRow(ProductEntity product, VariantEntity variant, WarehouseEntity warehouse, BigDecimal openingStock) {
        InventoryEntity saved = inventoryRepository.save(InventoryEntity.builder()
                .product(product).variant(variant).warehouse(warehouse).productStock(openingStock).build());
        saved.setInventoryStrId(StrIdGenerator.generate("INV", saved.getInventoryPrimeId()));
        return inventoryRepository.save(saved);
    }

    // Locks the existing row; if none exists for this warehouse, serialises creation by locking the parent first.
    public InventoryEntity lockOrCreateProductRow(String productStrId, WarehouseEntity warehouse) {
        return inventoryRepository.findProductRowForUpdate(productStrId, warehouse.getWarehouseStrId())
                .orElseGet(() -> {
                    ProductEntity product = productRepository.findByProductStrIdForUpdate(productStrId)
                            .orElseThrow(() -> new ResourceNotFoundException("No product found with ID '" + productStrId + "'."));
                    return inventoryRepository.findProductRowForUpdate(productStrId, warehouse.getWarehouseStrId())
                            .orElseGet(() -> createRow(product, null, warehouse, BigDecimal.ZERO));
                });
    }

    public InventoryEntity lockOrCreateVariantRow(String variantStrId, WarehouseEntity warehouse) {
        return inventoryRepository.findVariantRowForUpdate(variantStrId, warehouse.getWarehouseStrId())
                .orElseGet(() -> {
                    VariantEntity variant = variantRepository.findByVariantStrIdForUpdate(variantStrId)
                            .orElseThrow(() -> new ResourceNotFoundException("No variant found with ID '" + variantStrId + "'."));
                    return inventoryRepository.findVariantRowForUpdate(variantStrId, warehouse.getWarehouseStrId())
                            .orElseGet(() -> createRow(null, variant, warehouse, BigDecimal.ZERO));
                });
    }

    public BigDecimal productTotal(Long productPrimeId) {
        BigDecimal sum = inventoryRepository.sumStockByProductPrimeId(productPrimeId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public BigDecimal variantTotal(Long variantPrimeId) {
        BigDecimal sum = inventoryRepository.sumStockByVariantPrimeId(variantPrimeId);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public void recordProductHistory(ProductEntity product, WarehouseEntity warehouse, String changeType,
                                     BigDecimal previousStock, BigDecimal newStock, BigDecimal changeQty, String remarks) {
        persist(InventoryHistoryEntity.builder()
                .productPrimeId(product.getProductPrimeId())
                .productStrId(product.getProductStrId())
                .warehousePrimeId(warehouse.getWarehousePrimeId())
                .warehouseStrId(warehouse.getWarehouseStrId())
                .historyChangeType(changeType)
                .historyPreviousStock(previousStock)
                .historyNewStock(newStock)
                .historyChangeQty(changeQty)
                .historyRemarks(remarks)
                .build());
    }

    public void recordVariantHistory(VariantEntity variant, WarehouseEntity warehouse, String changeType,
                                     BigDecimal previousStock, BigDecimal newStock, BigDecimal changeQty, String remarks) {
        persist(InventoryHistoryEntity.builder()
                .productPrimeId(variant.getProduct().getProductPrimeId())
                .productStrId(variant.getProduct().getProductStrId())
                .variantPrimeId(variant.getVariantPrimeId())
                .variantStrId(variant.getVariantStrId())
                .warehousePrimeId(warehouse.getWarehousePrimeId())
                .warehouseStrId(warehouse.getWarehouseStrId())
                .historyChangeType(changeType)
                .historyPreviousStock(previousStock)
                .historyNewStock(newStock)
                .historyChangeQty(changeQty)
                .historyRemarks(remarks)
                .build());
    }

    private void persist(InventoryHistoryEntity history) {
        InventoryHistoryEntity saved = inventoryHistoryRepository.save(history);
        saved.setHistoryStrId(StrIdGenerator.generate("HIST", saved.getHistoryPrimeId()));
        inventoryHistoryRepository.save(saved);
    }
}