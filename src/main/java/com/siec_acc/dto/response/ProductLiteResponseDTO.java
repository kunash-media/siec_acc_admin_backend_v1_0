package com.siec_acc.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ProductLiteResponseDTO(
        Long productPrimeId,
        String productStrId,
        String productName,
        String productUnit,
        String productSize,
        BigDecimal productSellingPrice,
        BigDecimal productMrpPrice,
        String productVendorName,
        List<VariantLiteResponseDTO> variants
) {
    public ProductLiteResponseDTO(Long productPrimeId, String productStrId, String productName, String productUnit,
                                  String productSize, BigDecimal productSellingPrice, BigDecimal productMrpPrice,
                                  String productVendorName) {
        this(productPrimeId, productStrId, productName, productUnit, productSize,
                productSellingPrice, productMrpPrice, productVendorName, List.of());
    }

    public ProductLiteResponseDTO withVariants(List<VariantLiteResponseDTO> variants) {
        return new ProductLiteResponseDTO(productPrimeId, productStrId, productName, productUnit, productSize,
                productSellingPrice, productMrpPrice, productVendorName, variants);
    }
}