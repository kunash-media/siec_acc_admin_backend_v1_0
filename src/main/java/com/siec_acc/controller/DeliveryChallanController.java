package com.siec_acc.controller;

import com.siec_acc.dto.request.DeliveryChallanRequestDto;
import com.siec_acc.dto.response.DeliveryChallanResponseDto;
import com.siec_acc.dto.response.DeliveryChallanStatsResponseDto;
import com.siec_acc.dto.response.DeliveryChallanSummaryResponseDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.enum_status.DcStatus;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.service.DeliveryChallanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/delivery-challans")
public class DeliveryChallanController {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryChallanController.class);
    private final DeliveryChallanService dcService;

    public DeliveryChallanController(DeliveryChallanService dcService) {
        this.dcService = dcService;
    }

    // multipart: "dc" -> DeliveryChallanRequestDto JSON, "file" -> generated PDF (optional)
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeliveryChallanResponseDto> createDc(
            @RequestPart("dc") DeliveryChallanRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        byte[] fileBytes = null; String contentType = null; String fileName = null;
        if (file != null && !file.isEmpty()) {
            try {
                fileBytes = file.getBytes();
                contentType = file.getContentType();
                fileName = file.getOriginalFilename();
            } catch (IOException ex) {
                logger.error("Failed to read uploaded delivery challan PDF", ex);
                throw new FileProcessingException("We couldn't read the uploaded delivery challan PDF. Please try uploading the file again.");
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dcService.createDc(requestDto, fileBytes, contentType, fileName));
    }

    @PutMapping("/update/{dcPrimeId}")
    public ResponseEntity<DeliveryChallanResponseDto> updateDc(@PathVariable Long dcPrimeId,
                                                               @RequestBody DeliveryChallanRequestDto requestDto) {
        return ResponseEntity.ok(dcService.updateDc(dcPrimeId, requestDto));
    }

    @GetMapping("/get-dc/{dcPrimeId:\\d+}")
    public ResponseEntity<DeliveryChallanResponseDto> getDcById(@PathVariable Long dcPrimeId) {
        return ResponseEntity.ok(dcService.getDcById(dcPrimeId));
    }

    @GetMapping("/get-all-dcs")
    public ResponseEntity<PagedResponseDto<DeliveryChallanResponseDto>> getAllDcs(
            @RequestParam(required = false) DcStatus status,
            @RequestParam(required = false) Long clientPrimeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {
        return ResponseEntity.ok(dcService.getAllDcs(status, clientPrimeId, search, pageNumber, pageSize));
    }

    @DeleteMapping("/delete/{dcPrimeId}")
    public ResponseEntity<Void> deleteDc(@PathVariable Long dcPrimeId) {
        dcService.deleteDc(dcPrimeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<DeliveryChallanStatsResponseDto> getDcStats() {
        return ResponseEntity.ok(dcService.getDcStats());
    }

    // DRAFT -> DISPATCHED (reduces stock), DISPATCHED -> DELIVERED
    @PatchMapping("/patch-status/{dcPrimeId}")
    public ResponseEntity<DeliveryChallanResponseDto> updateStatus(@PathVariable Long dcPrimeId,
                                                                   @RequestParam DcStatus status) {
        return ResponseEntity.ok(dcService.updateDcStatus(dcPrimeId, status));
    }

    @PostMapping(value = "/{dcPrimeId}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DeliveryChallanResponseDto> uploadDcPdf(@PathVariable Long dcPrimeId,
                                                                  @RequestParam("file") MultipartFile file) {
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException ex) {
            logger.error("Failed to read uploaded PDF for dcPrimeId={}", dcPrimeId, ex);
            throw new FileProcessingException("We couldn't read the uploaded PDF. Please try uploading the file again.");
        }
        return ResponseEntity.ok(dcService.uploadDcPdf(dcPrimeId, fileBytes, file.getContentType(), file.getOriginalFilename()));
    }

    // dcPdfUrl points here: baseUrl + /api/v1/delivery-challans/{dcStrId}
    @GetMapping("/{dcStrId:.+}")
    public ResponseEntity<byte[]> downloadDcPdf(@PathVariable String dcStrId) {
        var file = dcService.downloadDcPdf(dcStrId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }

    @GetMapping("/dc-list")
    public ResponseEntity<PagedResponseDto<DeliveryChallanSummaryResponseDto>> getDcSummaries(
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {
        return ResponseEntity.ok(dcService.getDcSummaries(pageNumber, pageSize));
    }
}