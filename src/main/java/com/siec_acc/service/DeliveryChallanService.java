package com.siec_acc.service;

import com.siec_acc.dto.request.DeliveryChallanRequestDto;
import com.siec_acc.dto.response.*;
import com.siec_acc.enum_status.DcStatus;

public interface DeliveryChallanService {
    DeliveryChallanResponseDto createDc(DeliveryChallanRequestDto dto, byte[] fileBytes, String contentType, String fileName);
    DeliveryChallanResponseDto updateDc(Long dcPrimeId, DeliveryChallanRequestDto dto);
    DeliveryChallanResponseDto getDcById(Long dcPrimeId);
    PagedResponseDto<DeliveryChallanResponseDto> getAllDcs(DcStatus status, Long clientPrimeId, String search, int pageNumber, int pageSize);
    void deleteDc(Long dcPrimeId);
    DeliveryChallanStatsResponseDto getDcStats();
    DeliveryChallanResponseDto updateDcStatus(Long dcPrimeId, DcStatus status);
    DeliveryChallanResponseDto uploadDcPdf(Long dcPrimeId, byte[] fileBytes, String contentType, String fileName);
    FileDownloadDto downloadDcPdf(String dcStrId);
    PagedResponseDto<DeliveryChallanSummaryResponseDto> getDcSummaries(int pageNumber, int pageSize);
}