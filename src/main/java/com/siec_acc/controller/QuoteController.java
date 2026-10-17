package com.siec_acc.controller;

import com.siec_acc.dto.request.QuoteRequestDto;
import com.siec_acc.dto.response.*;
import com.siec_acc.enum_status.QuoteStatus;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.service.QuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private static final Logger logger = LoggerFactory.getLogger(QuoteController.class);

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    // multipart: "quote" -> QuoteRequestDto JSON, "file" -> generated PDF (optional)
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuoteResponseDto> createQuote(
            @RequestPart("quote") QuoteRequestDto requestDto,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        byte[] fileBytes = null;
        String contentType = null;
        String fileName = null;

        if (file != null && !file.isEmpty()) {
            try {
                fileBytes = file.getBytes();
                contentType = file.getContentType();
                fileName = file.getOriginalFilename();
            } catch (IOException ex) {
                logger.error("Failed to read uploaded quotation PDF", ex);
                throw new FileProcessingException("We couldn't read the uploaded quotation PDF. Please try uploading the file again.");
            }
        }

        QuoteResponseDto response = quoteService.createQuote(requestDto, fileBytes, contentType, fileName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{quotePrimeId}")
    public ResponseEntity<QuoteResponseDto> updateQuote(@PathVariable Long quotePrimeId,
                                                        @RequestBody QuoteRequestDto requestDto) {
        return ResponseEntity.ok(quoteService.updateQuote(quotePrimeId, requestDto));
    }

    @GetMapping("/get-quote/{quotePrimeId:\\d+}")
    public ResponseEntity<QuoteResponseDto> getQuoteById(@PathVariable Long quotePrimeId) {
        return ResponseEntity.ok(quoteService.getQuoteById(quotePrimeId));
    }

    @GetMapping("/get-all-quotes")
    public ResponseEntity<PagedResponseDto<QuoteResponseDto>> getAllQuotes(
            @RequestParam(required = false) QuoteStatus status,
            @RequestParam(required = false) Long clientPrimeId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "25") int pageSize) {
        return ResponseEntity.ok(quoteService.getAllQuotes(status, clientPrimeId, search, pageNumber, pageSize));
    }

    @DeleteMapping("/delete/{quotePrimeId}")
    public ResponseEntity<Void> deleteQuote(@PathVariable Long quotePrimeId) {
        quoteService.deleteQuote(quotePrimeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<QuoteStatsResponseDto> getQuoteStats() {
        return ResponseEntity.ok(quoteService.getQuoteStats());
    }

    // DRAFT -> SENT, SENT -> ACCEPTED / DECLINED
    @PatchMapping("/patch-status/{quotePrimeId}")
    public ResponseEntity<QuoteResponseDto> updateStatus(@PathVariable Long quotePrimeId,
                                                         @RequestParam QuoteStatus status) {
        return ResponseEntity.ok(quoteService.updateQuoteStatus(quotePrimeId, status));
    }

    @PatchMapping("/patch/{quotePrimeId}/convert-to-so")
    public ResponseEntity<QuoteResponseDto> convertToSalesOrder(@PathVariable Long quotePrimeId) {
        return ResponseEntity.ok(quoteService.convertToSalesOrder(quotePrimeId));
    }

    @PostMapping(value = "/{quotePrimeId}/upload-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuoteResponseDto> uploadQuotePdf(@PathVariable Long quotePrimeId,
                                                           @RequestParam("file") MultipartFile file) {
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException ex) {
            logger.error("Failed to read uploaded PDF for quotePrimeId={}", quotePrimeId, ex);
            throw new FileProcessingException("We couldn't read the uploaded PDF. Please try uploading the file again.");
        }
        return ResponseEntity.ok(quoteService.uploadQuotePdf(
                quotePrimeId, fileBytes, file.getContentType(), file.getOriginalFilename()));
    }

    // quotePdfUrl points here: baseUrl + /api/v1/quotes/{quoteStrId}
    @GetMapping("/{quoteStrId:.+}")
    public ResponseEntity<byte[]> downloadQuotePdf(@PathVariable String quoteStrId) {
        var file = quoteService.downloadQuotePdf(quoteStrId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .body(file.getData());
    }
}