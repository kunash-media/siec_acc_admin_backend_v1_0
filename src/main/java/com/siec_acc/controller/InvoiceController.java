package com.siec_acc.controller;

import com.siec_acc.dto.request.InvoiceRequestDto;
import com.siec_acc.dto.response.InvoiceResponseDto;
import com.siec_acc.dto.response.InvoiceStatsResponseDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.enum_status.InvoiceStatus;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.service.InvoiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Backs the "Invoices" tab on the frontend — CRUD, filters (status/customer/
 * search), pagination, stat cards, and record-payment.
 * Sending (email/WhatsApp) lives in EmailController, not here.
 */

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Frontend "New Invoice" form submits multipart/form-data with two parts:
    //   - "invoice" -> the form fields as JSON (InvoiceRequestDto)
    //   - "file"    -> the generated invoice PDF (optional; can be uploaded
    //                  later via /{invoicePrimeId}/upload-pdf instead)
    // Both are saved together in one call instead of create-then-upload.
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceResponseDto> createInvoice(
            @RequestPart("invoice") InvoiceRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        logger.info("Create invoice request received (customerId={}, itemCount={}, fileAttached={})",
                requestDto.getInvoiceCustomerId(),
                requestDto.getInvoiceItems() != null ? requestDto.getInvoiceItems().size() : 0,
                file != null && !file.isEmpty());

        byte[] fileBytes = null;
        String contentType = null;
        String fileName = null;

        if (file != null && !file.isEmpty()) {
            try {
                fileBytes = file.getBytes();
                contentType = file.getContentType();
                fileName = file.getOriginalFilename();
            } catch (IOException ex) {
                logger.error("Failed to read uploaded PDF while creating invoice (customerId={})",
                        requestDto.getInvoiceCustomerId(), ex);
                throw new FileProcessingException("We couldn't read the uploaded invoice PDF. Please try uploading the file again.");
            }
        }

        InvoiceResponseDto response = invoiceService.createInvoice(requestDto, fileBytes, contentType, fileName);
        logger.info("Invoice created successfully: invoiceStrId={}, invoicePrimeId={}",
                response.getInvoiceStrId(), response.getInvoicePrimeId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{invoicePrimeId}")
    public ResponseEntity<InvoiceResponseDto> updateInvoice(@PathVariable Long invoicePrimeId,
                                                            @RequestBody InvoiceRequestDto requestDto) {
        logger.info("Update invoice request received (invoicePrimeId={})", invoicePrimeId);
        InvoiceResponseDto response = invoiceService.updateInvoice(invoicePrimeId, requestDto);
        logger.info("Invoice updated successfully: invoiceStrId={}", response.getInvoiceStrId());
        return ResponseEntity.ok(response);
    }

    // Numeric-only constraint so this doesn't collide with the
    // "/{invoiceStrId}" PDF-download route below (invoiceStrId is non-numeric,
    // e.g. "INV-25-26-001").
    @GetMapping("get-invoice/{invoicePrimeId:\\d+}")
    public ResponseEntity<InvoiceResponseDto> getInvoiceById(@PathVariable Long invoicePrimeId) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(invoicePrimeId));
    }

    // Mirrors the frontend filter dropdown (status), customer filter, and
    // top search box — plus 1-based pageNumber/pageSize for the pagination controls.
    @GetMapping("/get-all-invoices")
    public ResponseEntity<PagedResponseDto<InvoiceResponseDto>> getAllInvoices(
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {
        return ResponseEntity.ok(invoiceService.getAllInvoices(status, customerId, search, pageNumber, pageSize));
    }

    @DeleteMapping("/delete/{invoicePrimeId}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long invoicePrimeId) {
        logger.info("Delete invoice request received (invoicePrimeId={})", invoicePrimeId);
        invoiceService.deleteInvoice(invoicePrimeId);
        return ResponseEntity.noContent().build();
    }

    // Backs the Total / Due / Overdue / Total Value stat cards.
    @GetMapping("/stats")
    public ResponseEntity<InvoiceStatsResponseDto> getInvoiceStats() {
        return ResponseEntity.ok(invoiceService.getInvoiceStats());
    }

    @PatchMapping("/patch/{invoicePrimeId}/mark-sent")
    public ResponseEntity<InvoiceResponseDto> markAsSent(@PathVariable Long invoicePrimeId) {
        logger.info("Mark-as-sent request received (invoicePrimeId={})", invoicePrimeId);
        return ResponseEntity.ok(invoiceService.markAsSent(invoicePrimeId));
    }

    @PatchMapping("patch-payment/{invoicePrimeId}/record-payment")
    public ResponseEntity<InvoiceResponseDto> recordPayment(@PathVariable Long invoicePrimeId,
                                                            @RequestParam BigDecimal amount) {
        logger.info("Record-payment request received (invoicePrimeId={}, amount={})", invoicePrimeId, amount);
        return ResponseEntity.ok(invoiceService.recordPayment(invoicePrimeId, amount));
    }

    // Replaces the stored PDF on an EXISTING invoice (stored as LONGBLOB).
    // For a brand-new invoice, prefer /create above — it accepts the file
    // in the same request instead of a separate call.
    @PostMapping(value = "/{invoicePrimeId}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InvoiceResponseDto> uploadInvoicePdf(@PathVariable Long invoicePrimeId,
                                                               @RequestParam("file") MultipartFile file) {
        logger.info("Upload-PDF request received (invoicePrimeId={}, fileName={})", invoicePrimeId, file.getOriginalFilename());

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException ex) {
            logger.error("Failed to read uploaded PDF for invoicePrimeId={}", invoicePrimeId, ex);
            throw new FileProcessingException("We couldn't read the uploaded PDF. Please try uploading the file again.");
        }

        InvoiceResponseDto response = invoiceService.uploadInvoicePdf(
                invoicePrimeId, fileBytes, file.getContentType(), file.getOriginalFilename());
        return ResponseEntity.ok(response);
    }

    // Streams the stored PDF back. This is the endpoint invoicePdfUrl points
    // to — hit as baseUrl + "/api/v1/invoices/{invoiceStrId}".
    @GetMapping("/{invoiceStrId:.+}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable String invoiceStrId) {
        var file = invoiceService.downloadInvoicePdf(invoiceStrId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }
}