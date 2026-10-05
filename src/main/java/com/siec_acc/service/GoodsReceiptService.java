package com.siec_acc.service;

import com.siec_acc.dto.request.GoodsReceiptRequestDto;
import com.siec_acc.dto.response.GoodsReceiptResponseDto;
import com.siec_acc.entity.PurchaseOrderEntity;

import java.util.List;

public interface GoodsReceiptService {
    GoodsReceiptResponseDto createGoodsReceipt(GoodsReceiptRequestDto requestDto);
    GoodsReceiptResponseDto updateGoodsReceipt(String grnStrId, GoodsReceiptRequestDto requestDto);
    void deleteGoodsReceipt(String grnStrId);
    GoodsReceiptResponseDto getGoodsReceiptByStrId(String grnStrId);
    List<GoodsReceiptResponseDto> getAllGoodsReceipts();
    List<GoodsReceiptResponseDto> getGoodsReceiptsByPo(String poStrId);

    /** Recomputes every GRN's status and the PO's status (partial / fully_received) from the received quantities. */
    void resyncPo(PurchaseOrderEntity po);
}