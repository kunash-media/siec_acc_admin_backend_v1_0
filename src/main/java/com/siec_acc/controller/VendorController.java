package com.siec_acc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siec_acc.dto.request.VendorRequestDto;
import com.siec_acc.dto.response.VendorResponseDto;
import com.siec_acc.service.VendorService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/vendor")
public class VendorController {
    private static final Logger logger = LoggerFactory.getLogger(VendorController.class);

    private final VendorService service;

    // Vendor-module-only parser. No shared Spring ObjectMapper bean/config change.
    private final ObjectMapper objectMapper = new ObjectMapper();

    public VendorController(VendorService service) {
        this.service = service;
    }

    @PostMapping(value = "/create-vendor", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestPart("vendor") String vendorJson,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes)
    {
        logger.info("POST /api/vendor/create-vendor -> Creating vendor");
        try {
            VendorRequestDto dto = parseVendorJson(vendorJson);
            dto.setDocumentTypes(documentTypes);
            VendorResponseDto response = service.createVendorWithDocuments(dto, documents);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Vendor create validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody("Vendor validation failed", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while creating vendor", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Internal server error", e.getMessage()));
        }
    }

    @GetMapping("/get-vendor-by-vendorId/{vendorId}")
    public ResponseEntity<?> getById(@PathVariable String vendorId) {
        logger.info("GET /api/vendor/get-vendor-by-vendorId/{} -> Fetching vendor", vendorId);
        return execute(() -> service.getVendorByVendorId(vendorId));
    }

    @GetMapping("/get-all-vendor")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        logger.info("GET /api/vendor/get-all-vendor -> page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);
        return execute(() -> service.getAllVendors(page, size, sortBy, sortDir));
    }

    @PutMapping(value = "/update-vendor-by-vendorId/{vendorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable String vendorId,
            @RequestPart("vendor") String vendorJson,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes)
    {

        logger.info("PUT /api/vendor/update-vendor-by-vendorId/{} -> Updating vendor",vendorJson);
        try {
            VendorRequestDto dto = parseVendorJson(vendorJson);
            dto.setDocumentTypes(documentTypes);
            return ResponseEntity.ok(service.updateVendorWithDocuments(vendorId, dto, documents));
        } catch (NoSuchElementException e) {
            logger.warn("Vendor not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Vendor update validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody("Vendor validation failed", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while updating vendor: {}", vendorJson, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Internal server error", e.getMessage()));
        }
    }

    @PatchMapping(value = "/patch-vendor-by-vendorId/{vendorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> patch(
            @PathVariable String vendorId,
            @RequestPart("vendor") String vendorJson,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents,
            @RequestParam(value = "documentTypes", required = false) List<String> documentTypes)
    {

        logger.info("PATCH /api/vendor/patch-vendor-by-vendorId/{} -> Partial update", vendorJson);
        try {
            VendorRequestDto dto = parseVendorJson(vendorJson);
            return ResponseEntity.ok(service.patchVendor(vendorId, dto, documents));
        } catch (NoSuchElementException e) {
            logger.warn("Vendor not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.warn("Vendor patch validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(errorBody("Vendor validation failed", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while patching vendor: {}", vendorJson, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Internal server error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-vendor-by-vendorId/{vendorId}")
    public ResponseEntity<?> delete(@PathVariable String vendorId) {
        logger.info("DELETE /api/vendor/delete-vendor-by-vendorId/{} -> Deleting vendor", vendorId);
        try {
            service.deleteVendor(vendorId);
            return ResponseEntity.ok(Map.of("message", "Vendor deleted successfully"));
        } catch (NoSuchElementException e) {
            logger.warn("Vendor not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    private VendorRequestDto parseVendorJson(String vendorJson) {
        if (vendorJson == null || vendorJson.isBlank()) {
            throw new IllegalArgumentException("vendor is required");
        }
        try {
            return objectMapper.readValue(vendorJson, VendorRequestDto.class);
        } catch (Exception e) {
            logger.warn("Invalid vendor JSON: {}", e.getMessage());
            throw new IllegalArgumentException("vendor must contain valid JSON");
        }
    }
    @GetMapping("/vendor-list")
    public ResponseEntity<?> getlist() {
        logger.info("GET /api/vendor/vendor-list -> Fetching all vendor list");
        return execute(service::getAllVendorList);
    }

    @GetMapping("/document/{vendorId}/{documentId}")
    public ResponseEntity<?> getDocument(
            @PathVariable String vendorId,
            @PathVariable Long documentId) {
        try {
            VendorService.DocumentContent document = service.getVendorDocument(vendorId, documentId);
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            try {
                if (document.getContentType() != null) {
                    mediaType = MediaType.parseMediaType(document.getContentType());
                }
            } catch (Exception ignored) {
                logger.warn("Invalid stored content type for documentId={}", documentId);
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .body(document.getData());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody(e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error while reading vendor document: vendorId={}, documentId={}",
                    vendorId, documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Internal server error", e.getMessage()));
        }
    }

    @DeleteMapping("/document/{vendorId}/{documentId}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable String vendorId,
            @PathVariable Long documentId) {
        try {
            service.deleteVendorDocument(vendorId, documentId);
            return ResponseEntity.ok(Map.of("message", "Vendor document deleted successfully"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody(e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error while deleting vendor document: vendorId={}, documentId={}",
                    vendorId, documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Internal server error", e.getMessage()));
        }
    }

    private Map<String, String> errorBody(String error, String details) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("error", error);
        if (details != null && !details.isBlank()) body.put("details", details);
        return body;
    }

    private ResponseEntity<?> execute(SupplierWithException action) {
        try {
            return ResponseEntity.ok(action.get());
        } catch (NoSuchElementException e) {
            logger.warn("Vendor not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error while processing vendor request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Internal server error", e.getMessage()));
        }
    }

    @FunctionalInterface
    private interface SupplierWithException {
        Object get();
    }
}