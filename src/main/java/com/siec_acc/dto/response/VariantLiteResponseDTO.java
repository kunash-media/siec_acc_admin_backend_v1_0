package com.siec_acc.dto.response;

import java.math.BigDecimal;

public record VariantLiteResponseDTO(
        Long variantPrimeId,
        String variantStrId,
        String productStrId,
        String variantName,
        BigDecimal variantMrpPrice,
        BigDecimal variantSellingPrice,
        String variantSku,
        String variantUnit,
        String variantSize
) {}