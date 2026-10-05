package com.siec_acc.controller;

import com.siec_acc.dto.request.ProformaRequestDto;
import com.siec_acc.dto.response.InvoiceResponseDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.dto.response.ProformaResponseDto;
import com.siec_acc.dto.response.ProformaStatsResponseDto;
import com.siec_acc.enum_status.ProformaStatus;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.service.ProformaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Backs the "Proforma Invoices" tab — CRUD, filters (OPEN/CONVERTED/DECLINED),
 * pagination, stat cards, and the convert-to-invoice action.
 */
@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/proformas")
public class ProformaController {

    private static final Logger logger = LoggerFactory.getLogger(ProformaController.class);

    private final ProformaService proformaService;

    public ProformaController(ProformaService proformaService) {
        this.proformaService = proformaService;
    }

    // Frontend "New Proforma" form submits multipart/form-data with two parts:
    //   - "proforma" -> the form fields as JSON (ProformaRequestDto)
    //   - "file"     -> the generated proforma PDF (optional; can be uploaded
    //                   later via /{proformaPrimeId}/upload-pdf instead)
    // Both are saved together in one call instead of create-then-upload.
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProformaResponseDto> createProforma(
            @RequestPart("proforma") @Valid ProformaRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        logger.info("Create proforma request received (customerId={}, itemCount={}, fileAttached={})",
                requestDto.getProformaCustomerId(),
                requestDto.getProformaItems() != null ? requestDto.getProformaItems().size() : 0,
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
                logger.error("Failed to read uploaded PDF while creating proforma (customerId={})",
                        requestDto.getProformaCustomerId(), ex);
                throw new FileProcessingException("We couldn't read the uploaded proforma PDF. Please try uploading the file again.");
            }
        }

        ProformaResponseDto response = proformaService.createProforma(requestDto, fileBytes, contentType, fileName);
        logger.info("Proforma created successfully: proformaStrId={}, proformaPrimeId={}",
                response.getProformaStrId(), response.getProformaPrimeId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{proformaPrimeId}")
    public ResponseEntity<ProformaResponseDto> updateProforma(@PathVariable Long proformaPrimeId,
                                                              @Valid @RequestBody ProformaRequestDto requestDto) {
        logger.info("Update proforma request received (proformaPrimeId={})", proformaPrimeId);
        ProformaResponseDto response = proformaService.updateProforma(proformaPrimeId, requestDto);
        logger.info("Proforma updated successfully: proformaStrId={}", response.getProformaStrId());
        return ResponseEntity.ok(response);
    }

    // Numeric-only constraint so this doesn't collide with the
    // "/{proformaStrId}" PDF-download route below.
    @GetMapping("/get/{proformaPrimeId:\\d+}")
    public ResponseEntity<ProformaResponseDto> getProformaById(@PathVariable Long proformaPrimeId) {
        return ResponseEntity.ok(proformaService.getProformaById(proformaPrimeId));
    }

    @GetMapping("/get-all-proforma")
    public ResponseEntity<PagedResponseDto<ProformaResponseDto>> getAllProformas(
            @RequestParam(required = false) ProformaStatus status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {
        return ResponseEntity.ok(proformaService.getAllProformas(status, customerId, search, pageNumber, pageSize));
    }

    @DeleteMapping("/delete/{proformaPrimeId}")
    public ResponseEntity<Void> deleteProforma(@PathVariable Long proformaPrimeId) {
        logger.info("Delete proforma request received (proformaPrimeId={})", proformaPrimeId);
        proformaService.deleteProforma(proformaPrimeId);
        return ResponseEntity.noContent().build();
    }

    // Backs the Total / Open / Declined / Total Value stat cards on the Proforma tab.
    @GetMapping("/stats")
    public ResponseEntity<ProformaStatsResponseDto> getProformaStats() {
        return ResponseEntity.ok(proformaService.getProformaStats());
    }

    // Turns an OPEN proforma into a real Invoice (status UNPAID) and marks
    // this proforma CONVERTED — ties into invoiceConvertedFromProformaId.
    @PostMapping("/{proformaPrimeId}/convert-to-invoice")
    public ResponseEntity<InvoiceResponseDto> convertToInvoice(@PathVariable Long proformaPrimeId) {
        logger.info("Convert-to-invoice request received (proformaPrimeId={})", proformaPrimeId);
        return ResponseEntity.ok(proformaService.convertToInvoice(proformaPrimeId));
    }

    @PatchMapping("/patch/{proformaPrimeId}/decline")
    public ResponseEntity<ProformaResponseDto> declineProforma(@PathVariable Long proformaPrimeId) {
        logger.info("Decline proforma request received (proformaPrimeId={})", proformaPrimeId);
        return ResponseEntity.ok(proformaService.declineProforma(proformaPrimeId));
    }

    // Replaces the stored PDF on an EXISTING proforma (stored as LONGBLOB).
    // For a brand-new proforma, prefer the plain POST above — it accepts the
    // file in the same request instead of a separate call.
    @PostMapping(value = "/{proformaPrimeId}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProformaResponseDto> uploadProformaPdf(@PathVariable Long proformaPrimeId,
                                                                 @RequestParam("file") MultipartFile file) {
        logger.info("Upload-PDF request received (proformaPrimeId={}, fileName={})", proformaPrimeId, file.getOriginalFilename());

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException ex) {
            logger.error("Failed to read uploaded PDF for proformaPrimeId={}", proformaPrimeId, ex);
            throw new FileProcessingException("We couldn't read the uploaded PDF. Please try uploading the file again.");
        }

        ProformaResponseDto response = proformaService.uploadProformaPdf(
                proformaPrimeId, fileBytes, file.getContentType(), file.getOriginalFilename());
        return ResponseEntity.ok(response);
    }

    // Streams the stored PDF back. This is the endpoint proformaPdfUrl points
    // to — hit as baseUrl + "/api/v1/proformas/{proformaStrId}".
    @GetMapping("/{proformaStrId:.+}")
    public ResponseEntity<byte[]> downloadProformaPdf(@PathVariable String proformaStrId) {
        var file = proformaService.downloadProformaPdf(proformaStrId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }
}