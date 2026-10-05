package com.siec_acc.service;

import com.siec_acc.dto.request.ProformaRequestDto;
import com.siec_acc.dto.response.FileDownloadDto;
import com.siec_acc.dto.response.InvoiceResponseDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.dto.response.ProformaResponseDto;
import com.siec_acc.dto.response.ProformaStatsResponseDto;
import com.siec_acc.enum_status.ProformaStatus;

public interface ProformaService {

    /**
     * Plain create — no PDF.
     */
    ProformaResponseDto createProforma(ProformaRequestDto requestDto);

    /**
     * Create-with-PDF — backs the frontend's "New Proforma" form submit,
     * which sends the form JSON and the generated PDF together in one
     * multipart request. Saves the proforma, then (if fileBytes is
     * non-null) stores the PDF and sets proformaPdfUrl in the same call.
     */
    ProformaResponseDto createProforma(ProformaRequestDto requestDto, byte[] fileBytes, String contentType, String fileName);

    ProformaResponseDto updateProforma(Long proformaPrimeId, ProformaRequestDto requestDto);

    ProformaResponseDto getProformaById(Long proformaPrimeId);

    PagedResponseDto<ProformaResponseDto> getAllProformas(ProformaStatus status,
                                                          Long customerId,
                                                          String search,
                                                          int pageNumber,
                                                          int pageSize);

    void deleteProforma(Long proformaPrimeId);

    ProformaStatsResponseDto getProformaStats();

    /**
     * Converts an OPEN proforma into a new InvoiceEntity (status UNPAID),
     * sets proformaStatus = CONVERTED + proformaConvertedToInvoiceId,
     * and returns the newly created invoice.
     */
    InvoiceResponseDto convertToInvoice(Long proformaPrimeId);

    ProformaResponseDto declineProforma(Long proformaPrimeId);

    /**
     * Stores/replaces the generated PDF as a LONGBLOB against an existing
     * proforma and sets proformaPdfUrl (relative path "/api/v1/proformas/{proformaStrId}").
     * Kept for replacing the PDF on an already-created proforma; new-proforma
     * creation should go through createProforma(dto, fileBytes, ...) instead.
     */
    ProformaResponseDto uploadProformaPdf(Long proformaPrimeId, byte[] fileBytes, String contentType, String fileName);

    /**
     * Fetches the stored PDF by the human-readable proformaStrId — this is
     * what GET /api/v1/proformas/{proformaStrId} streams back.
     */
    FileDownloadDto downloadProformaPdf(String proformaStrId);
}