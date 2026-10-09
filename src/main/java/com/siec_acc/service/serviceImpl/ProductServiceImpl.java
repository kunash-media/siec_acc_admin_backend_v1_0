package com.siec_acc.service.serviceImpl;


import com.siec_acc.config.InventoryStockSupport;
import com.siec_acc.dto.response.ProductLiteResponseDTO;
import com.siec_acc.dto.response.SliceResponseDTO;
import com.siec_acc.dto.response.VariantLiteResponseDTO;
import com.siec_acc.entity.InventoryEntity;
import com.siec_acc.entity.InventoryHistoryEntity;
import com.siec_acc.entity.ProductEntity;
import com.siec_acc.entity.WarehouseEntity;
import com.siec_acc.exceptions.DuplicateResourceException;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.service.WarehouseService;
import com.siec_acc.utils.StrIdGenerator;

import com.siec_acc.repository.InventoryHistoryRepository;
import com.siec_acc.repository.InventoryRepository;
import com.siec_acc.dto.request.ProductRequestDTO;
import com.siec_acc.dto.response.ProductResponseDTO;

import com.siec_acc.repository.ProductRepository;
import com.siec_acc.service.ProductService;
import com.siec_acc.repository.VariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final VariantRepository variantRepository;
    private final WarehouseService warehouseService;
    private final InventoryStockSupport stockSupport;

    private static final int MAX_LITE_PAGE_SIZE = 100;

    public ProductServiceImpl(ProductRepository productRepository, InventoryRepository inventoryRepository, InventoryHistoryRepository inventoryHistoryRepository,
                              VariantRepository variantRepository, WarehouseService warehouseService, InventoryStockSupport stockSupport) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryHistoryRepository = inventoryHistoryRepository;
        this.variantRepository = variantRepository;
        this.warehouseService = warehouseService;
        this.stockSupport = stockSupport;
    }

    @Override
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO requestDTO) {
        logger.info("Creating new product with name: {}", requestDTO.getProductName());

        if (requestDTO.getProductSku() != null && !requestDTO.getProductSku().isBlank()
                && productRepository.existsByProductSkuIgnoreCase(requestDTO.getProductSku())) {
            logger.warn("Duplicate SKU while creating product: {}", requestDTO.getProductSku());
            throw new DuplicateResourceException(
                    "A product with SKU '" + requestDTO.getProductSku() + "' already exists.");
        }

        WarehouseEntity warehouse = warehouseService.resolveActiveWarehouse(requestDTO.getWarehouseStrId());

        ProductEntity product = ProductEntity.builder()
                .productName(requestDTO.getProductName())
                .productSku(requestDTO.getProductSku())
                .productCategory(requestDTO.getProductCategory())
                .productSubCategory(requestDTO.getProductSubCategory())
                .productHsnCode(requestDTO.getProductHsnCode())
                .productUnit(requestDTO.getProductUnit())
                .productHeight(requestDTO.getProductHeight())
                .productWidth(requestDTO.getProductWidth())
                .productLength(requestDTO.getProductLength())
                .productMaterialType(requestDTO.getProductMaterialType())
                .productSize(requestDTO.getProductSize())
                .productNumber(requestDTO.getProductNumber())
                .productDescription(requestDTO.getProductDescription())
                .productSellingPrice(requestDTO.getProductSellingPrice())
                .productMrpPrice(requestDTO.getProductMrpPrice())
                .productGstRate(requestDTO.getProductGstRate())
                .productVendorName(requestDTO.getProductVendorName())
                .productVendorCompany(requestDTO.getProductVendorCompany())
                .build();

        ProductEntity savedProduct = productRepository.save(product);
        savedProduct.setProductStrId(StrIdGenerator.generate("PRD", savedProduct.getProductPrimeId()));

        BigDecimal openingStock = requestDTO.getProductStock() != null ? requestDTO.getProductStock() : BigDecimal.ZERO;
        savedProduct.setProductStatus(computeStatus(openingStock));
        savedProduct = productRepository.save(savedProduct);

        stockSupport.createRow(savedProduct, null, warehouse, openingStock);

        stockSupport.recordProductHistory(savedProduct, warehouse, "CREATE", BigDecimal.ZERO, openingStock, openingStock, "ProductEntity created with opening stock");
        logger.info("ProductEntity created successfully: {}", savedProduct.getProductStrId());
        return mapToResponse(savedProduct, openingStock);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(String productStrId, ProductRequestDTO requestDTO) {
        logger.info("Updating product: {}", productStrId);
        ProductEntity product = getProductEntityOrThrow(productStrId);

        checkDuplicateSkuOnUpdate(product, requestDTO.getProductSku());

        product.setProductName(requestDTO.getProductName());
        product.setProductSku(requestDTO.getProductSku());
        product.setProductCategory(requestDTO.getProductCategory());
        product.setProductSubCategory(requestDTO.getProductSubCategory());
        product.setProductHsnCode(requestDTO.getProductHsnCode());
        product.setProductUnit(requestDTO.getProductUnit());
        product.setProductHeight(requestDTO.getProductHeight());
        product.setProductWidth(requestDTO.getProductWidth());
        product.setProductLength(requestDTO.getProductLength());
        product.setProductMaterialType(requestDTO.getProductMaterialType());
        product.setProductSize(requestDTO.getProductSize());
        product.setProductNumber(requestDTO.getProductNumber());
        product.setProductDescription(requestDTO.getProductDescription());
        product.setProductSellingPrice(requestDTO.getProductSellingPrice());
        product.setProductMrpPrice(requestDTO.getProductMrpPrice());
        product.setProductGstRate(requestDTO.getProductGstRate());
        product.setProductVendorName(requestDTO.getProductVendorName());
        product.setProductVendorCompany(requestDTO.getProductVendorCompany());

        ProductEntity updated = productRepository.save(product);
        BigDecimal currentStock = applyStockChangeIfAny(updated, requestDTO.getProductStock(), requestDTO.getWarehouseStrId(), "MANUAL_UPDATE", "Stock updated via full product update");        updated.setProductStatus(computeStatus(currentStock));
        productRepository.save(updated);

        logger.info("ProductEntity updated successfully: {}", productStrId);
        return mapToResponse(updated, currentStock);
    }

    @Override
    @Transactional
    public ProductResponseDTO patchProduct(String productStrId, ProductRequestDTO requestDTO) {
        logger.info("Patching product: {}", productStrId);
        ProductEntity product = getProductEntityOrThrow(productStrId);

        checkDuplicateSkuOnUpdate(product, requestDTO.getProductSku());

        if (requestDTO.getProductName() != null) product.setProductName(requestDTO.getProductName());
        if (requestDTO.getProductSku() != null) product.setProductSku(requestDTO.getProductSku());
        if (requestDTO.getProductCategory() != null) product.setProductCategory(requestDTO.getProductCategory());
        if (requestDTO.getProductSubCategory() != null) product.setProductSubCategory(requestDTO.getProductSubCategory());
        if (requestDTO.getProductHsnCode() != null) product.setProductHsnCode(requestDTO.getProductHsnCode());
        if (requestDTO.getProductUnit() != null) product.setProductUnit(requestDTO.getProductUnit());
        if (requestDTO.getProductHeight() != null) product.setProductHeight(requestDTO.getProductHeight());
        if (requestDTO.getProductWidth() != null) product.setProductWidth(requestDTO.getProductWidth());
        if (requestDTO.getProductLength() != null) product.setProductLength(requestDTO.getProductLength());
        if (requestDTO.getProductMaterialType() != null) product.setProductMaterialType(requestDTO.getProductMaterialType());
        if (requestDTO.getProductSize() != null) product.setProductSize(requestDTO.getProductSize());
        if (requestDTO.getProductNumber() != null) product.setProductNumber(requestDTO.getProductNumber());
        if (requestDTO.getProductDescription() != null) product.setProductDescription(requestDTO.getProductDescription());
        if (requestDTO.getProductSellingPrice() != null) product.setProductSellingPrice(requestDTO.getProductSellingPrice());
        if (requestDTO.getProductMrpPrice() != null) product.setProductMrpPrice(requestDTO.getProductMrpPrice());
        if (requestDTO.getProductGstRate() != null) product.setProductGstRate(requestDTO.getProductGstRate());
        if (requestDTO.getProductVendorName() != null) product.setProductVendorName(requestDTO.getProductVendorName());
        if (requestDTO.getProductVendorCompany() != null) product.setProductVendorCompany(requestDTO.getProductVendorCompany());

        ProductEntity patched = productRepository.save(product);
        BigDecimal currentStock = applyStockChangeIfAny(patched, requestDTO.getProductStock(), requestDTO.getWarehouseStrId(), "MANUAL_UPDATE", "Stock patched via partial product update");
        patched.setProductStatus(computeStatus(currentStock));
        productRepository.save(patched);

        logger.info("ProductEntity patched successfully: {}", productStrId);
        return mapToResponse(patched, currentStock);
    }

    @Override
    @Transactional
    public void deleteProduct(String productStrId) {
        logger.info("Deleting product: {}", productStrId);
        ProductEntity product = getProductEntityOrThrow(productStrId);

        variantRepository.findByProduct_ProductStrId(productStrId).forEach(v -> {
            inventoryRepository.findAllByVariant_VariantPrimeId(v.getVariantPrimeId()).forEach(inventoryRepository::delete);
            variantRepository.delete(v);
        });
        inventoryRepository.findAllByProduct_ProductPrimeId(product.getProductPrimeId()).forEach(inventoryRepository::delete);        productRepository.delete(product);

        logger.info("ProductEntity (with its variants and inventory) deleted successfully: {}", productStrId);
    }

    @Override
    public ProductResponseDTO getProductByStrId(String productStrId) {
        logger.info("Fetching product: {}", productStrId);
        ProductEntity product = getProductEntityOrThrow(productStrId);
        return mapToResponse(product, getCurrentStock(product));
    }

    @Override
    public List<ProductResponseDTO> getAllProducts() {
        logger.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(p -> mapToResponse(p, getCurrentStock(p)))
                .collect(Collectors.toList());
    }

    // ---------- helpers ----------

    private ProductEntity getProductEntityOrThrow(String productStrId) {
        return productRepository.findByProductStrId(productStrId)
                .orElseThrow(() -> {
                    logger.warn("ProductEntity not found: {}", productStrId);
                    return new ResourceNotFoundException("No product found with ID '" + productStrId + "'.");
                });
    }

    private void checkDuplicateSkuOnUpdate(ProductEntity existing, String newSku) {
        if (newSku != null && !newSku.isBlank()
                && !newSku.equalsIgnoreCase(existing.getProductSku())
                && productRepository.existsByProductSkuIgnoreCase(newSku)) {
            logger.warn("Duplicate SKU while updating product: {}", newSku);
            throw new DuplicateResourceException("A product with SKU '" + newSku + "' already exists.");
        }
    }

    private BigDecimal getCurrentStock(ProductEntity product) {
        return stockSupport.productTotal(product.getProductPrimeId());
    }


    private BigDecimal applyStockChangeIfAny(ProductEntity product, BigDecimal newStock, String warehouseStrId, String changeType, String remarks) {
        BigDecimal total = stockSupport.productTotal(product.getProductPrimeId());
        if (newStock == null) return total;

        boolean warehouseGiven = warehouseStrId != null && !warehouseStrId.isBlank();
        if (!warehouseGiven && newStock.compareTo(total) == 0) return total;

        List<InventoryEntity> rows = inventoryRepository.findAllByProduct_ProductPrimeId(product.getProductPrimeId());
        WarehouseEntity warehouse;
        if (warehouseGiven) {
            warehouse = warehouseService.resolveActiveWarehouse(warehouseStrId);
        } else if (rows.size() == 1) {
            warehouse = warehouseService.resolveActiveWarehouse(rows.get(0).getWarehouse().getWarehouseStrId());
        } else if (rows.isEmpty()) {
            warehouse = warehouseService.resolveActiveWarehouse(null);
        } else {
            throw new IllegalArgumentException("Product has stock in multiple warehouses. Specify warehouseStrId or use the stock add/reduce APIs.");
        }

        InventoryEntity inventory = stockSupport.lockOrCreateProductRow(product.getProductStrId(), warehouse);
        BigDecimal previousStock = inventory.getProductStock() != null ? inventory.getProductStock() : BigDecimal.ZERO;
        if (newStock.compareTo(previousStock) == 0) return total;

        inventory.setProductStock(newStock);
        inventoryRepository.save(inventory);

        stockSupport.recordProductHistory(product, warehouse, changeType, previousStock, newStock, newStock.subtract(previousStock), remarks);
        return stockSupport.productTotal(product.getProductPrimeId());
    }


    private String computeStatus(BigDecimal stock) {
        if (stock == null) stock = BigDecimal.ZERO;
        if (stock.compareTo(BigDecimal.valueOf(20)) > 0) return "green";
        if (stock.compareTo(BigDecimal.valueOf(5)) > 0) return "amber";
        return "red";
    }

    private ProductResponseDTO mapToResponse(ProductEntity product, BigDecimal stock) {
        return ProductResponseDTO.builder()
                .productPrimeId(product.getProductPrimeId())
                .productStrId(product.getProductStrId())
                .productName(product.getProductName())
                .productSku(product.getProductSku())
                .productCategory(product.getProductCategory())
                .productSubCategory(product.getProductSubCategory())
                .productHsnCode(product.getProductHsnCode())
                .productUnit(product.getProductUnit())
                .productHeight(product.getProductHeight())
                .productWidth(product.getProductWidth())
                .productLength(product.getProductLength())
                .productMaterialType(product.getProductMaterialType())
                .productSize(product.getProductSize())
                .productNumber(product.getProductNumber())
                .productDescription(product.getProductDescription())
                .productSellingPrice(product.getProductSellingPrice())
                .productMrpPrice(product.getProductMrpPrice())
                .productGstRate(product.getProductGstRate())
                .productVendorName(product.getProductVendorName())
                .productVendorCompany(product.getProductVendorCompany())
                .productStatus(product.getProductStatus())
                .productStock(stock)
                .productCreatedAt(product.getProductCreatedAt())
                .productUpdatedAt(product.getProductUpdatedAt())
                .build();
    }

    private String toLikePattern(String search) {
        if (search == null) return null;
        String trimmed = search.trim();
        if (trimmed.isEmpty()) return null;
        if (trimmed.length() > 100) trimmed = trimmed.substring(0, 100);
        String escaped = trimmed.toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_");
        return "%" + escaped + "%";
    }

    @Override
    @Transactional(readOnly = true)
    public SliceResponseDTO<ProductLiteResponseDTO> getProductList(int page, int size, String search) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_LITE_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        String pattern = toLikePattern(search);
        logger.info("Fetching product list | page={} | size={} | search={}", safePage, safeSize, pattern != null);

        Slice<ProductLiteResponseDTO> slice = pattern == null
                ? productRepository.findAllLite(pageable)
                : productRepository.searchLite(pattern, pageable);
        List<ProductLiteResponseDTO> rows = slice.getContent();

        Map<String, List<VariantLiteResponseDTO>> variantsByProduct = rows.isEmpty()
                ? Map.of()
                : variantRepository.findLiteByProductStrIds(
                        rows.stream().map(ProductLiteResponseDTO::productStrId).collect(Collectors.toList()))
                .stream().collect(Collectors.groupingBy(VariantLiteResponseDTO::productStrId));

        List<ProductLiteResponseDTO> content = rows.stream()
                .map(r -> r.withVariants(variantsByProduct.getOrDefault(r.productStrId(), List.of())))
                .collect(Collectors.toList());

        return new SliceResponseDTO<>(content, safePage, safeSize, slice.hasNext(), slice.hasNext() ? safePage + 1 : null);
    }
}