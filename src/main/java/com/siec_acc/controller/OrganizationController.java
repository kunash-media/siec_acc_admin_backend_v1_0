package com.siec_acc.controller;

import com.siec_acc.dto.request.OrganizationRequestDTO;
import com.siec_acc.dto.response.OrganizationResponseDTO;
import com.siec_acc.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);
    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> createOrganization(@RequestBody OrganizationRequestDTO request) {
        logger.info("POST /api/organization");
        return ResponseEntity.status(HttpStatus.CREATED).body(organizationService.createOrganization(request));
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponseDTO>> getAllOrganizations() {
        logger.info("GET /api/organization");
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<OrganizationResponseDTO> getOrganization(@PathVariable String organizationId) {
        logger.info("GET /api/organization/{}", organizationId);
        return ResponseEntity.ok(organizationService.getOrganization(organizationId));
    }

    @GetMapping("/{organizationId}/logo")
    public ResponseEntity<byte[]> getOrganizationLogo(@PathVariable String organizationId) {
        logger.info("GET /api/organization/{}/logo", organizationId);
        return organizationService.getOrganizationLogo(organizationId);
    }

    @PutMapping("/{organizationId}")
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(
            @PathVariable String organizationId,
            @RequestBody OrganizationRequestDTO request) {
        logger.info("PUT /api/organization/{}", organizationId);
        return ResponseEntity.ok(organizationService.updateOrganization(organizationId, request));
    }

    @PatchMapping("/{organizationId}")
    public ResponseEntity<OrganizationResponseDTO> patchOrganization(
            @PathVariable String organizationId,
            @RequestBody OrganizationRequestDTO request) {
        logger.info("PATCH /api/organization/{}", organizationId);
        return ResponseEntity.ok(organizationService.patchOrganization(organizationId, request));
    }

    @PostMapping(value = "/{organizationId}/documents", consumes = "multipart/form-data")
    public ResponseEntity<OrganizationResponseDTO> uploadDocument(
            @PathVariable String organizationId,
            @RequestParam("documentName") String documentName,
            @RequestParam("file") MultipartFile file) {
        logger.info("POST /api/organization/{}/documents", organizationId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.uploadDocument(organizationId, documentName, file));
    }

    @GetMapping("/{organizationId}/documents")
    public ResponseEntity<List<Map<String, Object>>> getDocuments(@PathVariable String organizationId) {
        logger.info("GET /api/organization/{}/documents", organizationId);
        return ResponseEntity.ok(organizationService.getDocuments(organizationId));
    }

    @GetMapping("/{organizationId}/documents/{documentId}/content")
    public ResponseEntity<byte[]> getDocumentContent(
            @PathVariable String organizationId,
            @PathVariable String documentId) {
        logger.info("GET /api/organization/{}/documents/{}/content", organizationId, documentId);
        return organizationService.getDocumentContent(organizationId, documentId);
    }

    @DeleteMapping("/{organizationId}/documents/{documentId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable String organizationId,
            @PathVariable String documentId) {
        logger.info("DELETE /api/organization/{}/documents/{}", organizationId, documentId);
        organizationService.deleteDocument(organizationId, documentId);
        return ResponseEntity.ok("Document deleted successfully");
    }

    @DeleteMapping("/{organizationId}")
    public ResponseEntity<String> deleteOrganization(@PathVariable String organizationId) {
        logger.info("DELETE /api/organization/{}", organizationId);
        organizationService.deleteOrganization(organizationId);
        return ResponseEntity.ok("Organization deleted successfully");
    }

    @GetMapping("/{organizationId}/bank-name")
    public ResponseEntity<Map<String, String>> getOrganizationBankName(@PathVariable String organizationId) {
        logger.info("GET /api/organization/{}/bank-name", organizationId);
        OrganizationResponseDTO organization = organizationService.getOrganization(organizationId);
        return ResponseEntity.ok(Map.of("bankName", organization.getBankName() == null ? "" : organization.getBankName()));
    }
}