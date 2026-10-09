package com.siec_acc.service;

import com.siec_acc.dto.request.InvoiceRequestDto;
import com.siec_acc.dto.response.*;
import com.siec_acc.enum_status.InvoiceStatus;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {

    /**
     * Plain create — no PDF. Used internally (e.g. ProformaService#convertToInvoice)
     * where there's no file to attach yet.
     */
    InvoiceResponseDto createInvoice(InvoiceRequestDto requestDto);

    /**
     * Create-with-PDF — backs the frontend's "New Invoice" form submit, which
     * sends the form JSON and the generated PDF together in one multipart
     * request. Saves the invoice, then (if fileBytes is non-null) stores the
     * PDF and sets invoicePdfUrl in the same call, so the UI gets a fully
     * ready InvoiceResponseDto back in one round trip.
     */
    InvoiceResponseDto createInvoice(InvoiceRequestDto requestDto, byte[] fileBytes, String contentType, String fileName);

    InvoiceResponseDto updateInvoice(Long invoicePrimeId, InvoiceRequestDto requestDto);

    InvoiceResponseDto getInvoiceById(Long invoicePrimeId);

    PagedResponseDto<InvoiceResponseDto> getAllInvoices(InvoiceStatus status,
                                                        Long customerId,
                                                        String search,
                                                        int pageNumber,
                                                        int pageSize);

    void deleteInvoice(Long invoicePrimeId);

    InvoiceStatsResponseDto getInvoiceStats();

    /**
     * Marks the invoice SENT and returns the response — called after
     * EmailService/WhatsApp dispatch succeeds from the controller layer.
     */
    InvoiceResponseDto markAsSent(Long invoicePrimeId);

    /**
     * Records a payment against invoicePaidAmount and flips status to
     * PARTIAL or PAID as the balance closes.
     */
    InvoiceResponseDto recordPayment(Long invoicePrimeId, BigDecimal amount);

    /**
     * Stores/replaces the generated PDF as a LONGBLOB against an existing
     * invoice and sets invoicePdfUrl (relative path "/api/v1/invoices/{invoiceStrId}").
     * Kept for replacing the PDF on an already-created invoice; new-invoice
     * creation should go through createInvoice(dto, fileBytes, ...) instead.
     */
    InvoiceResponseDto uploadInvoicePdf(Long invoicePrimeId, byte[] fileBytes, String contentType, String fileName);

    /**
     * Fetches the stored PDF by the human-readable invoiceStrId — this is
     * what GET /api/v1/invoices/{invoiceStrId} streams back.
     */
    FileDownloadDto downloadInvoicePdf(String invoiceStrId);

    PagedResponseDto<InvoiceSummaryResponseDto> getInvoiceSummaries(String search, int pageNumber, int pageSize);}