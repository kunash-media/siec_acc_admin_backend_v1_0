package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.VendorRequestDto;
import com.siec_acc.dto.request.VendorRequestDto.SecondaryContactDto;
import com.siec_acc.dto.response.VendorListDto;
import com.siec_acc.dto.response.VendorResponseDto;
import com.siec_acc.dto.response.VendorResponseDto.PagedResponseDto;
import com.siec_acc.dto.response.VendorResponseDto.DocumentDto;
import com.siec_acc.entity.VendorEntity;
import com.siec_acc.repository.VendorRepository;
import com.siec_acc.service.VendorService;

import org.springframework.data.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class VendorServiceImpl implements VendorService {
    private static final Logger logger = LoggerFactory.getLogger(VendorServiceImpl.class);

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf", "image/jpeg", "image/png", "image/jpg", "image/webp",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final VendorRepository vendorRepository;


    public VendorServiceImpl(VendorRepository vendorRepository) {
        this.vendorRepository = vendorRepository;
    }

    @Override
    @Transactional
    public VendorResponseDto createVendorWithDocuments(VendorRequestDto d, List<MultipartFile> documents) {
        logger.info("Creating vendor. vendorName={}", d != null ? d.getVendorName() : null);

        VendorEntity entity = createVendorEntity(d);
        entity.setVendorStrId(generateVendorStrId());
        saveDocuments(entity, documents, d != null ? d.getDocumentTypes() : null);

        VendorEntity saved = vendorRepository.save(entity);
        logger.info("Vendor created. vendorPrimeId={}, vendorStrId={}",
                saved.getVendorPrimeId(), saved.getVendorStrId());
        return toResponse(saved);
    }

    private VendorEntity createVendorEntity(VendorRequestDto d) {
        if (d == null || !StringUtils.hasText(d.getVendorName())) {
            throw new IllegalArgumentException("vendorName is required");
        }
        VendorEntity e = new VendorEntity();
        copy(d, e, true);
        return e;
    }

    private String generateVendorStrId() {
        Set<Long> usedNumbers = new HashSet<>();
        for (VendorEntity vendor : vendorRepository.findAll()) {
            String vendorStrId = vendor.getVendorStrId();
            if (!StringUtils.hasText(vendorStrId) || !vendorStrId.startsWith("VND-")) continue;
            try {
                usedNumbers.add(Long.parseLong(vendorStrId.substring(4)));
            } catch (NumberFormatException ignored) {
                logger.warn("Ignoring invalid vendorStrId: {}", vendorStrId);
            }
        }
        long nextNumber = 1;
        while (usedNumbers.contains(nextNumber)) nextNumber++;
        return String.format("VND-%03d", nextNumber);
    }

    @Override
    public VendorResponseDto getVendorByVendorId(String id) {
        logger.info("Fetching vendor with vendorStrId: {}", id);
        return toResponse(find(id));
    }

    @Override
    public PagedResponseDto<VendorResponseDto> getAllVendors(int page, int size, String sortBy, String sortDir) {
        logger.info("Fetching all vendors. page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
        int p = Math.max(page, 0), s = size < 1 ? 10 : Math.min(size, 100);
        Set<String> allowed = Set.of("vendorPrimeId", "vendorStrId", "vendorName", "companyName", "vendorType", "city", "state", "vendorIsActive", "outstanding", "createdAt");
        String field = allowed.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction dir = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<VendorEntity> result = vendorRepository.findAll(PageRequest.of(p, s, Sort.by(dir, field)));
        return PagedResponseDto.from(result.map(this::toResponse));
    }

    @Transactional
    @Override
    public VendorResponseDto updateVendorWithDocuments(String id, VendorRequestDto d, List<MultipartFile> documents) {
        logger.info("Full update requested for vendorStrId={}", id);

        VendorEntity e = find(id);
        copy(d, e, true);

        if (d != null && Boolean.TRUE.equals(d.getClearDocument())) {
            clearDocuments(e);
        }
        saveDocuments(e, documents, d != null ? d.getDocumentTypes() : null);

        VendorEntity saved = vendorRepository.save(e);

        logger.info(
                "Vendor updated. vendorPrimeId={}, vendorStrId={}",
                saved.getVendorPrimeId(),
                saved.getVendorStrId()
        );

        return toResponse(saved);
    }

    @Transactional
    @Override
    public VendorResponseDto patchVendor(String id, VendorRequestDto d, List<MultipartFile> documents) {
        logger.info("PATCH requested for vendorStrId: {}", id);

        VendorEntity e = find(id);
        copy(d, e, false);

        if (d != null && Boolean.TRUE.equals(d.getClearDocument())) {
            clearDocuments(e);
        }
        saveDocuments(e, documents, d != null ? d.getDocumentTypes() : null);

        return toResponse(vendorRepository.save(e));
    }
    @Transactional
    @Override
    public void deleteVendor(String id) {
        logger.info("Deleting vendor with vendorStrId: {}", id);
        VendorEntity e = find(id);
        vendorRepository.delete(e);
        logger.info("Vendor deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentContent getVendorDocument(String vendorId, Long documentId) {
        VendorEntity entity = find(vendorId);
        VendorEntity.VendorDocument document = findDocument(entity, documentId);

        if (document.getDocumentData() == null || document.getDocumentData().length == 0) {
            throw new NoSuchElementException("Document not found for vendor: " + vendorId);
        }

        logger.info("Vendor document fetched. vendorStrId={}, documentId={}, size={}",
                vendorId, documentId, document.getDocumentData().length);

        return new DocumentContent(
                document.getDocumentData(),
                document.getContentType(),
                document.getFileName()
        );
    }

    @Override
    @Transactional
    public void deleteVendorDocument(String vendorId, Long documentId) {
        VendorEntity entity = find(vendorId);
        VendorEntity.VendorDocument document = findDocument(entity, documentId);
        entity.getDocuments().remove(document);
        vendorRepository.save(entity);
        logger.info("Vendor document deleted. vendorStrId={}, documentId={}", vendorId, documentId);
    }


    @Transactional(readOnly = true)
    @Override
    public List<VendorListDto> getAllVendorList() {
        logger.info("Fetching vendor summary list (vendorPrimeId, vendorStrId, vendorName, companyName)");
        return vendorRepository.findAllVendorList();
    }

    private void saveDocuments(VendorEntity entity, List<MultipartFile> files, List<String> documentTypes) {
        if (files == null || files.isEmpty()) return;

        int index = 0;
        for (MultipartFile file : files) {
            if (!hasFile(file)) {
                index++;
                continue;
            }
            validateDocument(file);

            try {
                Long documentId = nextDocumentId(entity);
                String documentUrl = buildDocumentUrl(entity.getVendorStrId(), documentId);

                String documentType = documentTypes != null && index < documentTypes.size()
                        ? documentTypes.get(index) : "OTHER";

                entity.getDocuments().add(new VendorEntity.VendorDocument(
                        documentId,
                        StringUtils.hasText(documentType) ? documentType : "OTHER",
                        documentUrl,
                        StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "document")),
                        normalizeContentType(file),
                        file.getSize(),
                        file.getBytes()
                ));
                index++;

                logger.info("Vendor document stored. vendorStrId={}, documentId={}, fileName={}, size={}",
                        entity.getVendorStrId(), documentId, file.getOriginalFilename(), file.getSize());
            } catch (IOException ex) {
                logger.error("Failed to read vendor document. vendorStrId={}", entity.getVendorStrId(), ex);
                throw new IllegalArgumentException("Failed to process uploaded document");
            }
        }
    }

    private VendorEntity.VendorDocument findDocument(VendorEntity entity, Long documentId) {
        return entity.getDocuments().stream()
                .filter(d -> documentId != null && documentId.equals(d.getDocumentId()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Document not found with ID: " + documentId));
    }

    private String buildDocumentUrl(String vendorId, Long documentId) {
        return "/api/vendor/document/" + vendorId + "/" + documentId;
    }

    private Long nextDocumentId(VendorEntity entity) {
        return entity.getDocuments().stream()
                .map(VendorEntity.VendorDocument::getDocumentId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .map(id -> id + 1L)
                .orElse(1L);
    }

    private void clearDocuments(VendorEntity entity) {
        entity.getDocuments().clear();
        logger.info("Vendor documents cleared. vendorStrId={}", entity.getVendorStrId());
    }

    private String normalizeContentType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank() && !"multipart/form-data".equalsIgnoreCase(contentType)) {
            return contentType;
        }
        String name = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "")).toLowerCase(Locale.ROOT);
        if (name.endsWith(".pdf")) return "application/pdf";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".webp")) return "image/webp";
        if (name.endsWith(".doc")) return "application/msword";
        if (name.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/octet-stream";
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private void validateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was provided");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds 10 MB");
        }


        String contentType = file.getContentType();
        String normalizedType = contentType == null ? "" : contentType.toLowerCase(Locale.ROOT).trim();

        if ("multipart/form-data".equals(normalizedType)) {
            return;
        }

        // If a real MIME type is supplied, allow the supported document types.
        if (ALLOWED_TYPES.contains(normalizedType)) {
            return;
        }

        // Some clients omit the file MIME type. Validate by filename extension.
        String fileName = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), "")
        ).toLowerCase(Locale.ROOT);

        boolean allowedExtension = fileName.endsWith(".pdf")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")
                || fileName.endsWith(".webp")
                || fileName.endsWith(".doc")
                || fileName.endsWith(".docx");

        if (normalizedType.isBlank() && allowedExtension) {
            return;
        }

        throw new IllegalArgumentException("Unsupported file type: " + contentType);
    }

    private VendorEntity find(String id) {
        logger.debug("Looking up vendor with vendorStrId: {}", id);
        return vendorRepository.findByVendorStrId(id)
                .orElseThrow(() -> new NoSuchElementException("Vendor not found with ID: " + id));
    }

    private void copy(VendorRequestDto d, VendorEntity e, boolean full) {
        if (d == null) return;
        if (full || d.getVendorName() != null) e.setVendorName(d.getVendorName());
        if (full || d.getCompanyName() != null) e.setCompanyName(d.getCompanyName());
        if (full || d.getVendorType() != null) e.setVendorType(d.getVendorType());
        if (full || d.getEmail() != null) e.setEmail(d.getEmail());
        if (full || d.getPhone() != null) e.setPhone(d.getPhone());
        if (full || d.getAddress() != null) e.setAddress(d.getAddress());
        if (full || d.getCity() != null) e.setCity(d.getCity());
        if (full || d.getState() != null) e.setState(d.getState());
        if (full || d.getStateCode() != null) e.setStateCode(d.getStateCode());
        if (full || d.getPincode() != null) e.setPincode(d.getPincode());
        if (full || d.getGstin() != null) e.setGstin(d.getGstin());
        if (full || d.getPan() != null) e.setPan(d.getPan());
        if (full || d.getGstRegistered() != null) e.setGstRegistered(d.getGstRegistered());
        if (full || d.getBankName() != null) e.setBankName(d.getBankName());
        if (full || d.getBankAccount() != null) e.setBankAccount(d.getBankAccount());
        if (full || d.getBankIfsc() != null) e.setBankIfsc(d.getBankIfsc());
        if (full || d.getPaymentTerms() != null) e.setPaymentTerms(d.getPaymentTerms());
        if (full || d.getStatus() != null) e.setVendorIsActive(!"inactive".equalsIgnoreCase(d.getStatus()));
        if (full || d.getOutstanding() != null) e.setOutstanding(d.getOutstanding());
        if (full || d.getSecondaryContacts() != null) {
            List<VendorEntity.SecondaryContact> cs = new ArrayList<>();
            if (d.getSecondaryContacts() != null) {
                for (SecondaryContactDto c : d.getSecondaryContacts()) {
                    cs.add(new VendorEntity.SecondaryContact(c.getName(), c.getDesignation(), c.getMobile(), c.getEmail()));
                }
            }
            e.setSecondaryContacts(cs);
        }
    }

    private VendorResponseDto toResponse(VendorEntity e) {
        VendorResponseDto d = new VendorResponseDto();
        d.setVendorPrimeId(e.getVendorPrimeId());
        d.setVendorStrId(e.getVendorStrId());
        d.setVendorId(e.getVendorStrId());
        d.setVendorName(e.getVendorName());
        d.setCompanyName(e.getCompanyName());
        d.setVendorType(e.getVendorType());
        d.setEmail(e.getEmail());
        d.setPhone(e.getPhone());
        d.setAddress(e.getAddress());
        d.setCity(e.getCity());
        d.setState(e.getState());
        d.setStateCode(e.getStateCode());
        d.setPincode(e.getPincode());
        d.setGstin(e.getGstin());
        d.setPan(e.getPan());
        d.setGstRegistered(e.getGstRegistered());
        d.setBankName(e.getBankName());
        d.setBankAccount(e.getBankAccount());
        d.setBankIfsc(e.getBankIfsc());
        d.setPaymentTerms(e.getPaymentTerms());
        d.setStatus(Boolean.TRUE.equals(e.getVendorIsActive()) ? "active" : "inactive");
        d.setOutstanding(e.getOutstanding());
        d.setCreatedAt(e.getCreatedAt());
        List<SecondaryContactDto> cs = new ArrayList<>();
        if (e.getSecondaryContacts() != null) {
            for (VendorEntity.SecondaryContact c : e.getSecondaryContacts()) {
                cs.add(new SecondaryContactDto(c.getName(), c.getDesignation(), c.getMobile(), c.getEmail()));
            }
        }
        d.setSecondaryContacts(cs);
        List<DocumentDto> documents = new ArrayList<>();
        if (e.getDocuments() != null) {
            for (VendorEntity.VendorDocument doc : e.getDocuments()) {
                documents.add(new DocumentDto(
                        doc.getDocumentId(),
                        doc.getDocumentType(),
                        doc.getDocumentUrl(),
                        doc.getFileSize()
                ));
            }
        }
        d.setDocuments(documents);
        return d;
    }
}
