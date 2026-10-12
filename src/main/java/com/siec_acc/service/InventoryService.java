package com.siec_acc.service;

import com.siec_acc.dto.request.InventoryAdjustDTO;
import com.siec_acc.dto.request.InventoryStockUpdateDTO;
import com.siec_acc.dto.response.InventoryHistoryResponseDTO;
import com.siec_acc.dto.response.InventoryListItemDTO;
import com.siec_acc.dto.response.InventoryResponseDTO;
import com.siec_acc.dto.response.StockSummaryResponseDTO;

import java.util.List;

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

    List<InventoryListItemDTO> getAllInventory();
    List<InventoryHistoryResponseDTO> getHistory(String productStrId);
    InventoryResponseDTO adjustStock(String productStrId, InventoryAdjustDTO requestDTO);
}