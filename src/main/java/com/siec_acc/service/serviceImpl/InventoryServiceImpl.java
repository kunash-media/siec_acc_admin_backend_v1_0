package com.siec_acc.service.serviceImpl;

import com.siec_acc.config.InventoryStockSupport;
import com.siec_acc.dto.response.StockSummaryResponseDTO;
import com.siec_acc.entity.*;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.VariantRepository;
import com.siec_acc.service.WarehouseService;
import com.siec_acc.utils.StrIdGenerator;
import com.siec_acc.dto.response.InventoryResponseDTO;
import com.siec_acc.dto.request.InventoryStockUpdateDTO;
import com.siec_acc.repository.InventoryRepository;
import com.siec_acc.service.InventoryService;

import com.siec_acc.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;
    private final WarehouseService warehouseService;
    private final InventoryStockSupport stockSupport;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, ProductRepository productRepository,
                                VariantRepository variantRepository, WarehouseService warehouseService,
                                InventoryStockSupport stockSupport) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.warehouseService = warehouseService;
        this.stockSupport = stockSupport;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryByProductStrId(String productStrId, String warehouseStrId) {
        logger.info("Fetching inventory | productStrId={} | warehouse={}", productStrId, warehouseStrId);
        WarehouseEntity warehouse = warehouseService.resolveWarehouse(warehouseStrId);
        return mapToResponse(inventoryRepository
                .findByProduct_ProductStrIdAndWarehouse_WarehouseStrId(productStrId, warehouse.getWarehouseStrId())
                .orElseThrow(() -> new ResourceNotFoundException("No inventory record found for product ID '" + productStrId
                        + "' in warehouse '" + warehouse.getWarehouseStrId() + "'.")));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDTO getInventoryByVariantStrId(String variantStrId, String warehouseStrId) {
        logger.info("Fetching inventory | variantStrId={} | warehouse={}", variantStrId, warehouseStrId);
        WarehouseEntity warehouse = warehouseService.resolveWarehouse(warehouseStrId);
        return mapToResponse(inventoryRepository
                .findByVariant_VariantStrIdAndWarehouse_WarehouseStrId(variantStrId, warehouse.getWarehouseStrId())
                .orElseThrow(() -> new ResourceNotFoundException("No inventory record found for variant ID '" + variantStrId
                        + "' in warehouse '" + warehouse.getWarehouseStrId() + "'.")));
    }

    @Override
    @Transactional(readOnly = true)
    public StockSummaryResponseDTO getProductStockSummary(String productStrId) {
        if (productRepository.findByProductStrId(productStrId).isEmpty()) {
            throw new ResourceNotFoundException("No product found with ID '" + productStrId + "'.");
        }
        List<InventoryResponseDTO> rows = inventoryRepository.findAllByProduct_ProductStrId(productStrId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
        return StockSummaryResponseDTO.builder().productStrId(productStrId).totalStock(sum(rows)).warehouses(rows).build();
    }

    @Override
    @Transactional(readOnly = true)
    public StockSummaryResponseDTO getVariantStockSummary(String variantStrId) {
        VariantEntity variant = variantRepository.findByVariantStrId(variantStrId)
                .orElseThrow(() -> new ResourceNotFoundException("No variant found with ID '" + variantStrId + "'."));
        List<InventoryResponseDTO> rows = inventoryRepository.findAllByVariant_VariantStrId(variantStrId).stream()
                .map(this::mapToResponse).collect(Collectors.toList());
        return StockSummaryResponseDTO.builder().productStrId(variant.getProduct().getProductStrId())
                .variantStrId(variantStrId).totalStock(sum(rows)).warehouses(rows).build();
    }

    @Override
    @Transactional
    public InventoryResponseDTO addStock(String productStrId, InventoryStockUpdateDTO requestDTO) {
        logger.info("Adding stock | productStrId={} | warehouse={} | qty={}", productStrId, requestDTO.getWarehouseStrId(), requestDTO.getChangeQty());
        validateQty(requestDTO, "add");
        String changeType = resolveAddChangeType(requestDTO);
        WarehouseEntity warehouse = warehouseService.resolveActiveWarehouse(requestDTO.getWarehouseStrId());

        InventoryEntity inventory = stockSupport.lockOrCreateProductRow(productStrId, warehouse);
        BigDecimal previousStock = current(inventory);
        BigDecimal newStock = previousStock.add(requestDTO.getChangeQty());
        inventory.setProductStock(newStock);
        InventoryEntity saved = inventoryRepository.save(inventory);

        updateProductStatus(inventory.getProduct());
        stockSupport.recordProductHistory(inventory.getProduct(), warehouse, changeType, previousStock, newStock,
                requestDTO.getChangeQty(), requestDTO.getRemarks() != null ? requestDTO.getRemarks() : "Stock added");

        logger.info("Stock added successfully | productStrId={} | warehouse={} | newStock={}", productStrId, warehouse.getWarehouseStrId(), newStock);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponseDTO reduceStock(String productStrId, InventoryStockUpdateDTO requestDTO) {
        logger.info("Reducing stock | productStrId={} | warehouse={} | qty={}", productStrId, requestDTO.getWarehouseStrId(), requestDTO.getChangeQty());
        validateQty(requestDTO, "reduce");
        WarehouseEntity warehouse = warehouseService.resolveWarehouse(requestDTO.getWarehouseStrId());

        InventoryEntity inventory = inventoryRepository.findProductRowForUpdate(productStrId, warehouse.getWarehouseStrId())
                .orElseThrow(() -> new ResourceNotFoundException("No inventory record found for product ID '" + productStrId
                        + "' in warehouse '" + warehouse.getWarehouseStrId() + "'."));
        BigDecimal previousStock = current(inventory);

        if (previousStock.compareTo(requestDTO.getChangeQty()) < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + previousStock
                    + ", requested reduction: " + requestDTO.getChangeQty() + " (warehouse " + warehouse.getWarehouseStrId() + ")");
        }

        BigDecimal newStock = previousStock.subtract(requestDTO.getChangeQty());
        inventory.setProductStock(newStock);
        InventoryEntity saved = inventoryRepository.save(inventory);

        updateProductStatus(inventory.getProduct());
        stockSupport.recordProductHistory(inventory.getProduct(), warehouse, "STOCK_REDUCE", previousStock, newStock,
                requestDTO.getChangeQty().negate(), requestDTO.getRemarks() != null ? requestDTO.getRemarks() : "Stock reduced");

        logger.info("Stock reduced successfully | productStrId={} | warehouse={} | newStock={}", productStrId, warehouse.getWarehouseStrId(), newStock);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public InventoryResponseDTO addVariantStock(String variantStrId, InventoryStockUpdateDTO requestDTO) {
        return changeVariantStock(variantStrId, requestDTO, true);
    }

    @Override
    @Transactional
    public InventoryResponseDTO reduceVariantStock(String variantStrId, InventoryStockUpdateDTO requestDTO) {
        return changeVariantStock(variantStrId, requestDTO, false);
    }

    // ---------- helpers ----------

    private InventoryResponseDTO changeVariantStock(String variantStrId, InventoryStockUpdateDTO requestDTO, boolean add) {
        logger.info("{} variant stock | variantStrId={} | warehouse={} | qty={}", add ? "Adding" : "Reducing",
                variantStrId, requestDTO.getWarehouseStrId(), requestDTO.getChangeQty());
        validateQty(requestDTO, add ? "add" : "reduce");
        String addChangeType = add ? resolveAddChangeType(requestDTO) : null;
        WarehouseEntity warehouse = add
                ? warehouseService.resolveActiveWarehouse(requestDTO.getWarehouseStrId())
                : warehouseService.resolveWarehouse(requestDTO.getWarehouseStrId());

        InventoryEntity inventory = add
                ? stockSupport.lockOrCreateVariantRow(variantStrId, warehouse)
                : inventoryRepository.findVariantRowForUpdate(variantStrId, warehouse.getWarehouseStrId())
                .orElseThrow(() -> new ResourceNotFoundException("No inventory record found for variant ID '" + variantStrId
                        + "' in warehouse '" + warehouse.getWarehouseStrId() + "'."));
        BigDecimal previousStock = current(inventory);

        if (!add && previousStock.compareTo(requestDTO.getChangeQty()) < 0) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + previousStock
                    + ", requested reduction: " + requestDTO.getChangeQty() + " (warehouse " + warehouse.getWarehouseStrId() + ")");
        }

        BigDecimal newStock = add ? previousStock.add(requestDTO.getChangeQty()) : previousStock.subtract(requestDTO.getChangeQty());
        inventory.setProductStock(newStock);
        InventoryEntity saved = inventoryRepository.save(inventory);

        VariantEntity variant = saved.getVariant();
        variant.setVariantStock(stockSupport.variantTotal(variant.getVariantPrimeId()));

        stockSupport.recordVariantHistory(variant, warehouse, add ? addChangeType : "STOCK_REDUCE", previousStock, newStock,
                add ? requestDTO.getChangeQty() : requestDTO.getChangeQty().negate(),
                requestDTO.getRemarks() != null ? requestDTO.getRemarks() : (add ? "Variant stock added" : "Variant stock reduced"));

        return mapToResponse(saved);
    }

    private void validateQty(InventoryStockUpdateDTO requestDTO, String action) {
        if (requestDTO.getChangeQty() == null || requestDTO.getChangeQty().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Stock quantity to " + action + " must be greater than zero.");
        }
    }

    private String resolveAddChangeType(InventoryStockUpdateDTO requestDTO) {
        String source = requestDTO.getSource();
        if (source == null || source.isBlank()) return "STOCK_ADD";
        if ("VENDOR_PURCHASE".equalsIgnoreCase(source.trim())) return "VENDOR_PURCHASE_ADD";
        throw new IllegalArgumentException("Unsupported stock source '" + source + "'.");
    }

    private BigDecimal current(InventoryEntity inventory) {
        return inventory.getProductStock() != null ? inventory.getProductStock() : BigDecimal.ZERO;
    }

    private BigDecimal sum(List<InventoryResponseDTO> rows) {
        return rows.stream().map(InventoryResponseDTO::getProductStock).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // product status is based on the TOTAL across all warehouses
    private void updateProductStatus(ProductEntity product) {
        BigDecimal total = stockSupport.productTotal(product.getProductPrimeId());
        String status = total.compareTo(BigDecimal.valueOf(20)) > 0 ? "green"
                : total.compareTo(BigDecimal.valueOf(5)) > 0 ? "amber" : "red";
        product.setProductStatus(status);
        productRepository.save(product);
    }

    private InventoryResponseDTO mapToResponse(InventoryEntity inventory) {
        String productStrId = inventory.getProduct() != null
                ? inventory.getProduct().getProductStrId()
                : inventory.getVariant().getProduct().getProductStrId();
        return InventoryResponseDTO.builder()
                .inventoryPrimeId(inventory.getInventoryPrimeId())
                .inventoryStrId(inventory.getInventoryStrId())
                .productStrId(productStrId)
                .variantStrId(inventory.getVariant() != null ? inventory.getVariant().getVariantStrId() : null)
                .productStock(inventory.getProductStock())
                .inventoryUpdatedAt(inventory.getInventoryUpdatedAt())
                .warehouseStrId(inventory.getWarehouse().getWarehouseStrId())
                .warehouseName(inventory.getWarehouse().getWarehouseName())
                .build();
    }
}