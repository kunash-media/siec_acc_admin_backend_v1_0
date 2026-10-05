package com.siec_acc.service;

import com.siec_acc.dto.request.InventoryStockUpdateDTO;
import com.siec_acc.dto.response.InventoryResponseDTO;
import com.siec_acc.dto.response.StockSummaryResponseDTO;

public interface InventoryService {

    // product
    InventoryResponseDTO getInventoryByProductStrId(String productStrId, String warehouseStrId);
    InventoryResponseDTO addStock(String productStrId, InventoryStockUpdateDTO requestDTO);
    InventoryResponseDTO reduceStock(String productStrId, InventoryStockUpdateDTO requestDTO);
    StockSummaryResponseDTO getProductStockSummary(String productStrId);

    // variant
    InventoryResponseDTO getInventoryByVariantStrId(String variantStrId, String warehouseStrId);
    InventoryResponseDTO addVariantStock(String variantStrId, InventoryStockUpdateDTO requestDTO);
    InventoryResponseDTO reduceVariantStock(String variantStrId, InventoryStockUpdateDTO requestDTO);
    StockSummaryResponseDTO getVariantStockSummary(String variantStrId);
}