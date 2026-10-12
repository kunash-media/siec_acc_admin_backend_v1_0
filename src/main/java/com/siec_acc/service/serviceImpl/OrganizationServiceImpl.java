package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.OrganizationRequestDTO;
import com.siec_acc.dto.response.OrganizationResponseDTO;
import com.siec_acc.entity.OrganizationDocumentEntity;
import com.siec_acc.entity.OrganizationEntity;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.OrganizationDocumentRepository;
import com.siec_acc.repository.OrganizationRepository;
import com.siec_acc.service.OrganizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationServiceImpl.class);
    private static final long MAX_DOCUMENT_SIZE = 5L * 1024L * 1024L;
    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/png", "image/webp");

    private final OrganizationRepository organizationRepository;
    private final OrganizationDocumentRepository organizationDocumentRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository,
                                   OrganizationDocumentRepository organizationDocumentRepository) {
        this.organizationRepository = organizationRepository;
        this.organizationDocumentRepository = organizationDocumentRepository;
    }

    @Override
    @Transactional
    public OrganizationResponseDTO createOrganization(OrganizationRequestDTO request) {
        validateRequest(request);
        OrganizationEntity organization = organizationRepository.findAll().stream().findFirst().orElseGet(() -> {
            OrganizationEntity value = new OrganizationEntity();
            value.setOrganizationId(generateOrganizationId());
            return value;
        });
        copyFields(organization, request, false);
        saveLogoBlob(organization, request);
        return toResponse(organizationRepository.save(organization));
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponseDTO getOrganization(String organizationId) {
        return toResponse(findOrganization(organizationId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganizationResponseDTO> getAllOrganizations() {
        return organizationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public OrganizationResponseDTO updateOrganization(String organizationId, OrganizationRequestDTO request) {
        validateRequest(request);
        OrganizationEntity organization = findOrganization(organizationId);
        copyFields(organization, request, false);
        saveLogoBlob(organization, request);
        return toResponse(organizationRepository.save(organization));
    }

    @Override
    @Transactional
    public OrganizationResponseDTO patchOrganization(String organizationId, OrganizationRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("Organization request is required.");
        OrganizationEntity organization = findOrganization(organizationId);
        copyFields(organization, request, true);
        saveLogoBlob(organization, request);
        return toResponse(organizationRepository.save(organization));
    }

    @Override
    @Transactional
    public void deleteOrganization(String organizationId) {
        OrganizationEntity organization = findOrganization(organizationId);
        organizationDocumentRepository.deleteAllByOrganization_OrganizationPrimeId(organization.getOrganizationPrimeId());
        organizationRepository.delete(organization);
        logger.info("Organization deleted: {}", organizationId);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getOrganizationLogo(String organizationId) {
        byte[] logo = findOrganization(organizationId).getLogoBlob();
        if (logo == null || logo.length == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(detectImageType(logo)).body(logo);
    }

    @Override
    @Transactional
    public OrganizationResponseDTO uploadDocument(String organizationId, String documentName, MultipartFile file) {
        OrganizationEntity organization = findOrganization(organizationId);
        validateDocument(documentName, file);
        try {
            String documentId = generateDocumentId();
            OrganizationDocumentEntity document = new OrganizationDocumentEntity();
            document.setDocumentId(documentId);
            document.setOrganization(organization);
            document.setDocumentName(documentName.trim());
            document.setOriginalFileName(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
            document.setContentType(file.getContentType().toLowerCase(Locale.ROOT));
            document.setFileSize(file.getSize());
            document.setDocumentBlob(file.getBytes());
            document.setDocumentUrl("/api/organization/" + organizationId + "/documents/" + documentId + "/content");
            organizationDocumentRepository.save(document);
            logger.info("Organization document saved: {}", documentId);
            return toResponse(organization);
        } catch (IOException exception) {
            logger.error("Unable to read organization document", exception);
            throw new IllegalStateException("Unable to save document");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDocuments(String organizationId) {
        findOrganization(organizationId);
        return organizationDocumentRepository.findAllByOrganization_OrganizationIdOrderByCreatedAtDesc(organizationId)
                .stream().map(this::documentMetadata).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getDocumentContent(String organizationId, String documentId) {
        OrganizationDocumentEntity document = findDocument(organizationId, documentId);
        byte[] data = document.getDocumentBlob();
        if (data == null || data.length == 0) throw new ResourceNotFoundException("Document content not found");
        String contentType = document.getContentType() == null ? "application/octet-stream" : document.getContentType();
        String fileName = document.getOriginalFileName() == null ? "document" : document.getOriginalFileName();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName.replace("\"", "") + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(data.length)
                .body(data);
    }

    @Override
    @Transactional
    public void deleteDocument(String organizationId, String documentId) {
        OrganizationDocumentEntity document = findDocument(organizationId, documentId);
        organizationDocumentRepository.delete(document);
        logger.info("Organization document deleted: {}", documentId);
    }

    private void validateRequest(OrganizationRequestDTO request) {
        if (request == null) throw new IllegalArgumentException("Organization request is required.");
        if (request.getLegalName() == null || request.getLegalName().isBlank()) {
            throw new IllegalArgumentException("Legal company name is required.");
        }
    }

    private void validateDocument(String documentName, MultipartFile file) {
        if (documentName == null || documentName.isBlank()) throw new IllegalArgumentException("Document name is required");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Document file is required");
        if (file.getSize() > MAX_DOCUMENT_SIZE) throw new IllegalArgumentException("File size must not exceed 5 MB");
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_DOCUMENT_TYPES.contains(contentType)) throw new IllegalArgumentException("Only PDF, JPG, JPEG, PNG or WEBP files are allowed");
    }

    private OrganizationDocumentEntity findDocument(String organizationId, String documentId) {
        findOrganization(organizationId);
        return organizationDocumentRepository.findByDocumentIdAndOrganization_OrganizationId(documentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));
    }

    private Map<String, Object> documentMetadata(OrganizationDocumentEntity document) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("documentId", document.getDocumentId());
        metadata.put("documentName", document.getDocumentName());
        metadata.put("originalFileName", document.getOriginalFileName());
        metadata.put("contentType", document.getContentType());
        metadata.put("fileSize", document.getFileSize());
        metadata.put("documentUrl", document.getDocumentUrl());
        metadata.put("createdAt", document.getCreatedAt());
        return metadata;
    }

    private void saveLogoBlob(OrganizationEntity organization, OrganizationRequestDTO request) {
        String logoValue = request.getLogoUrl();
        if (logoValue == null || logoValue.isBlank() || !logoValue.startsWith("data:image/")) return;
        try {
            String[] parts = logoValue.split(",", 2);
            if (parts.length != 2) return;
            organization.setLogoBlob(java.util.Base64.getDecoder().decode(parts[1]));
            organization.setLogoUrl("/api/organization/" + organization.getOrganizationId() + "/logo");
        } catch (IllegalArgumentException exception) {
            logger.error("Unable to decode organization logo", exception);
            throw new IllegalStateException("Unable to save organization logo");
        }
    }

    private MediaType detectImageType(byte[] bytes) {
        if (bytes.length >= 8 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) return MediaType.IMAGE_PNG;
        if (bytes.length >= 3 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) return MediaType.IMAGE_JPEG;
        if (bytes.length >= 12 && bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F' && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') return MediaType.parseMediaType("image/webp");
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    private OrganizationEntity findOrganization(String organizationId) {
        if (organizationId == null || organizationId.isBlank()) throw new IllegalArgumentException("Organization ID is required.");
        return organizationRepository.findByOrganizationId(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + organizationId));
    }

    private String generateOrganizationId() {
        String organizationId;
        do { organizationId = "ORG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }
        while (organizationRepository.existsByOrganizationId(organizationId));
        return organizationId;
    }

    private String generateDocumentId() { return "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }

    private void copyFields(OrganizationEntity o, OrganizationRequestDTO r, boolean skipNulls) {
        if (!skipNulls || r.getCompanyName() != null) o.setCompanyName(trim(r.getCompanyName()));
        if (!skipNulls || r.getLegalName() != null) o.setLegalName(trim(r.getLegalName()));
        if (!skipNulls || r.getBusinessType() != null) o.setBusinessType(trim(r.getBusinessType()));
        if (!skipNulls || r.getAddress() != null) o.setAddress(trim(r.getAddress()));
        if (!skipNulls || r.getCity() != null) o.setCity(trim(r.getCity()));
        if (!skipNulls || r.getState() != null) o.setState(trim(r.getState()));
        if (!skipNulls || r.getCountry() != null) o.setCountry(trim(r.getCountry()));
        if (!skipNulls || r.getPincode() != null) o.setPincode(trim(r.getPincode()));
        if (!skipNulls || r.getPhone() != null) o.setPhone(trim(r.getPhone()));
        if (!skipNulls || r.getEmail() != null) o.setEmail(trim(r.getEmail()));
        if (!skipNulls || r.getWebsite() != null) o.setWebsite(trim(r.getWebsite()));
        if (!skipNulls || r.getBankName() != null) o.setBankName(trim(r.getBankName()));
        if (!skipNulls || r.getBankAccountNumber() != null) o.setBankAccountNumber(trim(r.getBankAccountNumber()));
        if (!skipNulls || r.getBankIfscCode() != null) o.setBankIfscCode(trim(r.getBankIfscCode()));
        if (!skipNulls || r.getLogoUrl() != null) o.setLogoUrl(trim(r.getLogoUrl()));
        if (!skipNulls || r.getGstRegistered() != null) o.setGstRegistered(r.getGstRegistered());
        if (!skipNulls || r.getGstNumber() != null) o.setGstNumber(trim(r.getGstNumber()));
        if (!skipNulls || r.getTaxRegistrationType() != null) o.setTaxRegistrationType(trim(r.getTaxRegistrationType()));
        if (!skipNulls || r.getTaxState() != null) o.setTaxState(trim(r.getTaxState()));
        if (!skipNulls || r.getDefaultTaxRate() != null) o.setDefaultTaxRate(trim(r.getDefaultTaxRate()));
        if (!skipNulls || r.getTaxInclusive() != null) o.setTaxInclusive(r.getTaxInclusive());
        if (!skipNulls || r.getDefaultUnit() != null) o.setDefaultUnit(trim(r.getDefaultUnit()));
        if (!skipNulls || r.getUnitSystem() != null) o.setUnitSystem(trim(r.getUnitSystem()));
        if (!skipNulls || r.getDecimalPrecision() != null) o.setDecimalPrecision(r.getDecimalPrecision());
        if (!skipNulls || r.getFinancialYearStart() != null) o.setFinancialYearStart(trim(r.getFinancialYearStart()));
        if (!skipNulls || r.getTimezone() != null) o.setTimezone(trim(r.getTimezone()));
        if (!skipNulls || r.getOrganizationStatus() != null) o.setOrganizationStatus(trim(r.getOrganizationStatus()));
    }

    private String trim(String value) { return value == null ? null : value.trim(); }

    private OrganizationResponseDTO toResponse(OrganizationEntity o) {
        OrganizationResponseDTO r = new OrganizationResponseDTO();
        r.setOrganizationId(o.getOrganizationId()); r.setCompanyName(o.getCompanyName()); r.setLegalName(o.getLegalName());
        r.setBusinessType(o.getBusinessType()); r.setAddress(o.getAddress()); r.setCity(o.getCity()); r.setState(o.getState());
        r.setCountry(o.getCountry()); r.setPincode(o.getPincode()); r.setPhone(o.getPhone()); r.setEmail(o.getEmail());
        r.setWebsite(o.getWebsite()); r.setLogoUrl(o.getLogoUrl()); r.setBankName(o.getBankName());
        r.setBankAccountNumber(o.getBankAccountNumber()); r.setBankIfscCode(o.getBankIfscCode());
        r.setGstRegistered(o.getGstRegistered()); r.setGstNumber(o.getGstNumber()); r.setTaxRegistrationType(o.getTaxRegistrationType());
        r.setTaxState(o.getTaxState()); r.setDefaultTaxRate(o.getDefaultTaxRate()); r.setTaxInclusive(o.getTaxInclusive());
        r.setDefaultUnit(o.getDefaultUnit()); r.setUnitSystem(o.getUnitSystem()); r.setDecimalPrecision(o.getDecimalPrecision());
        r.setFinancialYearStart(o.getFinancialYearStart()); r.setTimezone(o.getTimezone()); r.setOrganizationStatus(o.getOrganizationStatus());
        r.setCreatedAt(o.getCreatedAt()); r.setUpdatedAt(o.getUpdatedAt());
        return r;
    }
}