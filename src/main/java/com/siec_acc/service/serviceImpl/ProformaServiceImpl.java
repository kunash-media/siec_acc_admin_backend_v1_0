package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.InvoiceRequestDto;
import com.siec_acc.dto.request.InvoiceItemRequestDto;
import com.siec_acc.dto.request.ProformaItemRequestDto;
import com.siec_acc.dto.request.ProformaRequestDto;
import com.siec_acc.dto.response.FileDownloadDto;
import com.siec_acc.dto.response.InvoiceResponseDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.dto.response.ProformaItemResponseDto;
import com.siec_acc.dto.response.ProformaResponseDto;
import com.siec_acc.dto.response.ProformaStatsResponseDto;
import com.siec_acc.entity.ProformaEntity;
import com.siec_acc.entity.ProformaItemEntity;
import com.siec_acc.enum_status.InvoiceStatus;
import com.siec_acc.enum_status.ProformaStatus;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.exceptions.InvalidInvoiceStatusException;
import com.siec_acc.exceptions.InvoiceItemRequiredException;
import com.siec_acc.exceptions.ProformaNotFoundException;
import com.siec_acc.repository.ProformaRepository;
import com.siec_acc.service.InvoiceService;
import com.siec_acc.service.ProformaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProformaServiceImpl implements ProformaService {

    private static final Logger logger = LoggerFactory.getLogger(ProformaServiceImpl.class);

    private final ProformaRepository proformaRepository;
    // Reused here only for the convertToInvoice() flow — keeps invoice
    // creation logic (numbering, totals) in one place rather than duplicated.
    private final InvoiceService invoiceService;

    public ProformaServiceImpl(ProformaRepository proformaRepository, InvoiceService invoiceService) {
        this.proformaRepository = proformaRepository;
        this.invoiceService = invoiceService;
    }

    // ---------------------------------------------------------------
    // CREATE (plain — no PDF)
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public ProformaResponseDto createProforma(ProformaRequestDto requestDto) {
        if (requestDto.getProformaItems() == null || requestDto.getProformaItems().isEmpty()) {
            logger.warn("Create proforma rejected — no line items provided (customerId={})", requestDto.getProformaCustomerId());
            throw new InvoiceItemRequiredException("Please add at least one item to the proforma invoice before saving.");
        }

        String proformaStrId = generateNextProformaNumber();
        logger.info("Creating proforma {} for customerId={}", proformaStrId, requestDto.getProformaCustomerId());

        ProformaEntity entity = ProformaEntity.builder()
                .proformaStrId(proformaStrId)
                // Download URL for the stored PDF — GET {baseUrl}/api/v1/proformas/{proformaStrId}
                .proformaPdfUrl("/api/v1/proformas/" + proformaStrId)
                .proformaDate(requestDto.getProformaDate())
                .proformaValidUntil(requestDto.getProformaValidUntil())
                .proformaCustomerId(requestDto.getProformaCustomerId())
                .proformaCustomerName(requestDto.getProformaCustomerName())
                .proformaCustomerGst(requestDto.getProformaCustomerGst())
                .proformaCustomerEmail(requestDto.getProformaCustomerEmail())
                .proformaCustomerState(requestDto.getProformaCustomerState())
                .proformaCustomerStateCode(requestDto.getProformaCustomerStateCode())
                .proformaCustomerAddress(requestDto.getProformaCustomerAddress())
                .proformaShippingAddress(requestDto.getProformaShippingAddress())
                .proformaCurrency(requestDto.getProformaCurrency())
                .proformaExchangeRate(defaultIfNull(requestDto.getProformaExchangeRate(), BigDecimal.ONE))
                .proformaDiscountPercent(defaultIfNull(requestDto.getProformaDiscountPercent(), BigDecimal.ZERO))
                .proformaTaxPercent(defaultIfNull(requestDto.getProformaTaxPercent(), BigDecimal.ZERO))
                .proformaShippingCharges(defaultIfNull(requestDto.getProformaShippingCharges(), BigDecimal.ZERO))
                .proformaOtherCharges(defaultIfNull(requestDto.getProformaOtherCharges(), BigDecimal.ZERO))
                .proformaRoundOff(defaultIfNull(requestDto.getProformaRoundOff(), BigDecimal.ZERO))
                .proformaStatus(requestDto.getProformaStatus() != null ? requestDto.getProformaStatus() : ProformaStatus.OPEN)
                .proformaPlaceOfSupply(requestDto.getProformaPlaceOfSupply())
                .proformaTerms(requestDto.getProformaTerms())
                .proformaNotes(requestDto.getProformaNotes())
                .proformaReferenceNumber(requestDto.getProformaReferenceNumber())
                .proformaPoNumber(requestDto.getProformaPoNumber())
                .proformaAttachmentUrl(requestDto.getProformaAttachmentUrl())
                .proformaSignatureUrl(requestDto.getProformaSignatureUrl())
                .proformaAuthorizedSignatory(requestDto.getProformaAuthorizedSignatory())
                .build();

        attachItems(entity, requestDto.getProformaItems());
        applyTotals(entity);

        ProformaEntity saved = proformaRepository.save(entity);
        logger.info("Proforma {} created successfully (proformaPrimeId={})", saved.getProformaStrId(), saved.getProformaPrimeId());
        return toResponseDto(saved);
    }

    // ---------------------------------------------------------------
    // CREATE + PDF  (backs the frontend form submit: JSON + file together)
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public ProformaResponseDto createProforma(ProformaRequestDto requestDto, byte[] fileBytes, String contentType, String fileName) {
        ProformaResponseDto created = createProforma(requestDto);

        if (fileBytes != null && fileBytes.length > 0) {
            logger.info("Attaching PDF ({} bytes) to newly created proforma {}", fileBytes.length, created.getProformaStrId());
            created = uploadProformaPdf(created.getProformaPrimeId(), fileBytes, contentType, fileName);
        } else {
            logger.info("No PDF attached at creation time for proforma {} — can be uploaded later via upload-pdf", created.getProformaStrId());
        }

        return created;
    }

    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public ProformaResponseDto updateProforma(Long proformaPrimeId, ProformaRequestDto requestDto) {
        ProformaEntity entity = findEntityOrThrow(proformaPrimeId);

        if (requestDto.getProformaItems() == null || requestDto.getProformaItems().isEmpty()) {
            logger.warn("Update proforma rejected — no line items provided (proformaPrimeId={})", proformaPrimeId);
            throw new InvoiceItemRequiredException("Please add at least one item to the proforma invoice before saving.");
        }

        entity.setProformaDate(requestDto.getProformaDate());
        entity.setProformaValidUntil(requestDto.getProformaValidUntil());
        entity.setProformaCustomerId(requestDto.getProformaCustomerId());
        entity.setProformaCustomerName(requestDto.getProformaCustomerName());
        entity.setProformaCustomerGst(requestDto.getProformaCustomerGst());
        entity.setProformaCustomerEmail(requestDto.getProformaCustomerEmail());
        entity.setProformaCustomerState(requestDto.getProformaCustomerState());
        entity.setProformaCustomerStateCode(requestDto.getProformaCustomerStateCode());
        entity.setProformaCustomerAddress(requestDto.getProformaCustomerAddress());
        entity.setProformaShippingAddress(requestDto.getProformaShippingAddress());
        entity.setProformaCurrency(requestDto.getProformaCurrency());
        entity.setProformaExchangeRate(defaultIfNull(requestDto.getProformaExchangeRate(), BigDecimal.ONE));
        entity.setProformaDiscountPercent(defaultIfNull(requestDto.getProformaDiscountPercent(), BigDecimal.ZERO));
        entity.setProformaTaxPercent(defaultIfNull(requestDto.getProformaTaxPercent(), BigDecimal.ZERO));
        entity.setProformaShippingCharges(defaultIfNull(requestDto.getProformaShippingCharges(), BigDecimal.ZERO));
        entity.setProformaOtherCharges(defaultIfNull(requestDto.getProformaOtherCharges(), BigDecimal.ZERO));
        entity.setProformaRoundOff(defaultIfNull(requestDto.getProformaRoundOff(), BigDecimal.ZERO));
        if (requestDto.getProformaStatus() != null) {
            if (entity.getProformaStatus() == ProformaStatus.CONVERTED) {
                logger.warn("Rejected status change on already-converted proforma {}", entity.getProformaStrId());
                throw new InvalidInvoiceStatusException(
                        "Proforma " + entity.getProformaStrId() + " is already converted and its status cannot be changed.");
            }
            entity.setProformaStatus(requestDto.getProformaStatus());
        }
        entity.setProformaPlaceOfSupply(requestDto.getProformaPlaceOfSupply());
        entity.setProformaTerms(requestDto.getProformaTerms());
        entity.setProformaNotes(requestDto.getProformaNotes());
        entity.setProformaReferenceNumber(requestDto.getProformaReferenceNumber());
        entity.setProformaPoNumber(requestDto.getProformaPoNumber());
        entity.setProformaAttachmentUrl(requestDto.getProformaAttachmentUrl());
        entity.setProformaSignatureUrl(requestDto.getProformaSignatureUrl());
        entity.setProformaAuthorizedSignatory(requestDto.getProformaAuthorizedSignatory());

        entity.getProformaItems().clear();
        attachItems(entity, requestDto.getProformaItems());
        applyTotals(entity);

        ProformaEntity saved = proformaRepository.save(entity);
        logger.info("Proforma {} updated successfully (proformaPrimeId={})", saved.getProformaStrId(), saved.getProformaPrimeId());
        return toResponseDto(saved);
    }

    // ---------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public ProformaResponseDto getProformaById(Long proformaPrimeId) {
        return toResponseDto(findEntityOrThrow(proformaPrimeId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<ProformaResponseDto> getAllProformas(ProformaStatus status,
                                                                 Long customerId,
                                                                 String search,
                                                                 int pageNumber,
                                                                 int pageSize) {
        int safePage = Math.max(pageNumber, 1) - 1;
        int safeSize = pageSize <= 0 ? 25 : pageSize;

        Page<ProformaEntity> page = proformaRepository.search(
                status, customerId, blankToNull(search),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "proformaCreatedAt")));

        List<ProformaResponseDto> content = page.getContent().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return PagedResponseDto.<ProformaResponseDto>builder()
                .content(content)
                .pageNumber(pageNumber)
                .pageSize(safeSize)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    // ---------------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public void deleteProforma(Long proformaPrimeId) {
        ProformaEntity entity = findEntityOrThrow(proformaPrimeId);
        proformaRepository.delete(entity);
        logger.info("Proforma {} deleted (proformaPrimeId={})", entity.getProformaStrId(), proformaPrimeId);
    }

    // ---------------------------------------------------------------
    // STATS  (backs the Total / Open / Declined / Total Value stat cards)
    // ---------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public ProformaStatsResponseDto getProformaStats() {
        long total = proformaRepository.count();
        long open = proformaRepository.countByProformaStatus(ProformaStatus.OPEN);
        long declined = proformaRepository.countByProformaStatus(ProformaStatus.DECLINED);
        BigDecimal totalValue = proformaRepository.sumTotalValueInr();

        return ProformaStatsResponseDto.builder()
                .totalCount(total)
                .openCount(open)
                .declinedCount(declined)
                .totalValueInr(totalValue)
                .build();
    }

    // ---------------------------------------------------------------
    // CONVERT TO INVOICE
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public InvoiceResponseDto convertToInvoice(Long proformaPrimeId) {
        ProformaEntity proforma = findEntityOrThrow(proformaPrimeId);

        if (proforma.getProformaStatus() != ProformaStatus.OPEN) {
            logger.warn("Rejected convert-to-invoice for proforma {} — status is {}", proforma.getProformaStrId(), proforma.getProformaStatus());
            throw new InvalidInvoiceStatusException(
                    "Only an OPEN proforma can be converted to an invoice. Current status: " + proforma.getProformaStatus() + ".");
        }

        List<InvoiceItemRequestDto> itemDtos = proforma.getProformaItems().stream()
                .map(i -> InvoiceItemRequestDto.builder()
                        .invoiceItemProductId(i.getProformaItemProductId())
                        .invoiceItemName(i.getProformaItemName())
                        .invoiceItemHsnCode(i.getProformaItemHsnCode())
                        .invoiceItemUnit(i.getProformaItemUnit())
                        .invoiceItemQty(i.getProformaItemQty())
                        .invoiceItemRate(i.getProformaItemRate())
                        .invoiceItemGstPercent(i.getProformaItemGstPercent())
                        .build())
                .collect(Collectors.toList());

        InvoiceRequestDto invoiceRequest = InvoiceRequestDto.builder()
                .invoiceDate(LocalDate.now())
                .invoiceDueDate(LocalDate.now().plusDays(15))
                .invoiceCustomerId(proforma.getProformaCustomerId())
                .invoiceCustomerName(proforma.getProformaCustomerName())
                .invoiceCustomerGst(proforma.getProformaCustomerGst())
                .invoiceCustomerEmail(proforma.getProformaCustomerEmail())
                .invoiceCustomerState(proforma.getProformaCustomerState())
                .invoiceCustomerStateCode(proforma.getProformaCustomerStateCode())
                .invoiceCustomerAddress(proforma.getProformaCustomerAddress())
                .invoiceShippingAddress(proforma.getProformaShippingAddress())
                .invoiceCurrency(proforma.getProformaCurrency())
                .invoiceExchangeRate(proforma.getProformaExchangeRate())
                .invoiceItems(itemDtos)
                .invoiceDiscountPercent(proforma.getProformaDiscountPercent())
                .invoiceTaxPercent(proforma.getProformaTaxPercent())
                .invoiceShippingCharges(proforma.getProformaShippingCharges())
                .invoiceOtherCharges(proforma.getProformaOtherCharges())
                .invoiceRoundOff(proforma.getProformaRoundOff())
                .invoiceStatus(InvoiceStatus.UNPAID)
                .invoicePlaceOfSupply(proforma.getProformaPlaceOfSupply())
                .invoiceTerms(proforma.getProformaTerms())
                .invoiceReferenceNumber(proforma.getProformaReferenceNumber())
                .invoicePoNumber(proforma.getProformaPoNumber())
                .invoiceConvertedFromProformaId(proforma.getProformaPrimeId())
                .build();

        InvoiceResponseDto createdInvoice = invoiceService.createInvoice(invoiceRequest);

        proforma.setProformaStatus(ProformaStatus.CONVERTED);
        proforma.setProformaConvertedToInvoiceId(createdInvoice.getInvoicePrimeId());
        proformaRepository.save(proforma);

        logger.info("Proforma {} converted to invoice {}", proforma.getProformaStrId(), createdInvoice.getInvoiceStrId());
        return createdInvoice;
    }

    @Override
    @Transactional
    public ProformaResponseDto declineProforma(Long proformaPrimeId) {
        ProformaEntity entity = findEntityOrThrow(proformaPrimeId);
        if (entity.getProformaStatus() == ProformaStatus.CONVERTED) {
            logger.warn("Rejected decline for already-converted proforma {}", entity.getProformaStrId());
            throw new InvalidInvoiceStatusException(
                    "Proforma " + entity.getProformaStrId() + " is already converted and cannot be declined.");
        }
        entity.setProformaStatus(ProformaStatus.DECLINED);
        logger.info("Proforma {} declined", entity.getProformaStrId());
        return toResponseDto(proformaRepository.save(entity));
    }

    // ---------------------------------------------------------------
    // PDF STORAGE  (LONGBLOB) — served back via GET /api/v1/proformas/{proformaStrId}
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public ProformaResponseDto uploadProformaPdf(Long proformaPrimeId, byte[] fileBytes, String contentType, String fileName) {
        ProformaEntity entity = findEntityOrThrow(proformaPrimeId);

        if (fileBytes == null || fileBytes.length == 0) {
            logger.warn("Rejected PDF upload for proforma {} — empty file", entity.getProformaStrId());
            throw new FileProcessingException("The uploaded PDF appears to be empty. Please choose a valid file.");
        }

        entity.setProformaPdfBlob(fileBytes);
        entity.setProformaPdfContentType(contentType != null ? contentType : "application/pdf");
        entity.setProformaPdfFileName(fileName != null ? fileName : entity.getProformaStrId() + ".pdf");
        entity.setProformaPdfUrl(buildRelativeUrl(entity.getProformaStrId()));

        ProformaEntity saved = proformaRepository.save(entity);
        logger.info("PDF stored for proforma {} ({} bytes)", saved.getProformaStrId(), fileBytes.length);
        return toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadProformaPdf(String proformaStrId) {
        ProformaEntity entity = proformaRepository.findByProformaStrId(proformaStrId)
                .orElseThrow(() -> {
                    logger.warn("PDF download requested for unknown proforma number {}", proformaStrId);
                    return new ProformaNotFoundException("We couldn't find a proforma invoice with number " + proformaStrId + ".");
                });

        if (entity.getProformaPdfBlob() == null) {
            logger.warn("PDF download requested but no file stored yet for proforma {}", proformaStrId);
            throw new ProformaNotFoundException("No PDF has been generated yet for proforma " + proformaStrId + ".");
        }

        return FileDownloadDto.builder()
                .data(entity.getProformaPdfBlob())
                .contentType(entity.getProformaPdfContentType() != null ? entity.getProformaPdfContentType() : "application/pdf")
                .fileName(entity.getProformaPdfFileName() != null ? entity.getProformaPdfFileName() : proformaStrId + ".pdf")
                .build();
    }

    // Relative path only — frontend/consumer prepends its own base URL.
    // e.g. baseUrl + "/api/v1/proformas/PI-25-26-001" -> downloads the PDF.
    private String buildRelativeUrl(String proformaStrId) {
        return "/api/v1/proformas/" + proformaStrId;
    }

    private ProformaEntity findEntityOrThrow(Long proformaPrimeId) {
        return proformaRepository.findById(proformaPrimeId)
                .orElseThrow(() -> {
                    logger.warn("Proforma not found for proformaPrimeId={}", proformaPrimeId);
                    return new ProformaNotFoundException("We couldn't find that proforma invoice. It may have been deleted.");
                });
    }

    private void attachItems(ProformaEntity entity, List<ProformaItemRequestDto> itemDtos) {
        List<ProformaItemEntity> items = new ArrayList<>();
        for (ProformaItemRequestDto dto : itemDtos) {
            BigDecimal amount = dto.getProformaItemQty().multiply(dto.getProformaItemRate());
            ProformaItemEntity item = ProformaItemEntity.builder()
                    .proforma(entity)
                    .proformaItemProductId(dto.getProformaItemProductId())
                    .proformaItemName(dto.getProformaItemName())
                    .proformaItemHsnCode(dto.getProformaItemHsnCode())
                    .proformaItemUnit(dto.getProformaItemUnit())
                    .proformaItemQty(dto.getProformaItemQty())
                    .proformaItemRate(dto.getProformaItemRate())
                    .proformaItemGstPercent(defaultIfNull(dto.getProformaItemGstPercent(), BigDecimal.ZERO))
                    .proformaItemAmount(amount)
                    .build();
            items.add(item);
        }
        entity.getProformaItems().addAll(items);
    }

    private void applyTotals(ProformaEntity entity) {
        BigDecimal subtotal = entity.getProformaItems().stream()
                .map(ProformaItemEntity::getProformaItemAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmt = subtotal.multiply(entity.getProformaDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discountAmt);

        BigDecimal taxAmt = afterDiscount.multiply(entity.getProformaTaxPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal total = afterDiscount.add(taxAmt)
                .add(entity.getProformaShippingCharges())
                .add(entity.getProformaOtherCharges())
                .add(entity.getProformaRoundOff());

        entity.setProformaSubTotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        entity.setProformaTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        entity.setProformaTotalAmountInr(total.multiply(entity.getProformaExchangeRate()).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * "PI-25-26-00X" style number, scoped to the current Indian financial year.
     */
    private String generateNextProformaNumber() {
        int currentYear = Year.now().getValue();
        int fyStart = LocalDate.now().getMonthValue() >= 4 ? currentYear : currentYear - 1;
        String fyLabel = String.valueOf(fyStart).substring(2) + "-" + String.valueOf(fyStart + 1).substring(2);
        long countThisYear = proformaRepository.count() + 1;
        return String.format("PI-%s-%03d", fyLabel, countThisYear);
    }

    private ProformaResponseDto toResponseDto(ProformaEntity entity) {
        List<ProformaItemResponseDto> items = entity.getProformaItems().stream()
                .map(i -> ProformaItemResponseDto.builder()
                        .proformaItemPrimeId(i.getProformaItemPrimeId())
                        .proformaItemProductId(i.getProformaItemProductId())
                        .proformaItemName(i.getProformaItemName())
                        .proformaItemHsnCode(i.getProformaItemHsnCode())
                        .proformaItemUnit(i.getProformaItemUnit())
                        .proformaItemQty(i.getProformaItemQty())
                        .proformaItemRate(i.getProformaItemRate())
                        .proformaItemGstPercent(i.getProformaItemGstPercent())
                        .proformaItemAmount(i.getProformaItemAmount())
                        .build())
                .collect(Collectors.toList());

        return ProformaResponseDto.builder()
                .proformaPrimeId(entity.getProformaPrimeId())
                .proformaStrId(entity.getProformaStrId())
                .proformaDate(entity.getProformaDate())
                .proformaValidUntil(entity.getProformaValidUntil())
                .proformaCustomerId(entity.getProformaCustomerId())
                .proformaCustomerName(entity.getProformaCustomerName())
                .proformaCustomerGst(entity.getProformaCustomerGst())
                .proformaCustomerEmail(entity.getProformaCustomerEmail())
                .proformaCustomerState(entity.getProformaCustomerState())
                .proformaCustomerStateCode(entity.getProformaCustomerStateCode())
                .proformaCustomerAddress(entity.getProformaCustomerAddress())
                .proformaShippingAddress(entity.getProformaShippingAddress())
                .proformaCurrency(entity.getProformaCurrency())
                .proformaExchangeRate(entity.getProformaExchangeRate())
                .proformaSubTotal(entity.getProformaSubTotal())
                .proformaDiscountPercent(entity.getProformaDiscountPercent())
                .proformaTaxPercent(entity.getProformaTaxPercent())
                .proformaShippingCharges(entity.getProformaShippingCharges())
                .proformaOtherCharges(entity.getProformaOtherCharges())
                .proformaRoundOff(entity.getProformaRoundOff())
                .proformaTotalAmount(entity.getProformaTotalAmount())
                .proformaTotalAmountInr(entity.getProformaTotalAmountInr())
                .proformaStatus(entity.getProformaStatus())
                .proformaPlaceOfSupply(entity.getProformaPlaceOfSupply())
                .proformaTerms(entity.getProformaTerms())
                .proformaNotes(entity.getProformaNotes())
                .proformaReferenceNumber(entity.getProformaReferenceNumber())
                .proformaPoNumber(entity.getProformaPoNumber())
                .proformaPdfUrl(entity.getProformaPdfUrl())
                .proformaAttachmentUrl(entity.getProformaAttachmentUrl())
                .proformaSignatureUrl(entity.getProformaSignatureUrl())
                .proformaAuthorizedSignatory(entity.getProformaAuthorizedSignatory())
                .proformaConvertedToInvoiceId(entity.getProformaConvertedToInvoiceId())
                .proformaItems(items)
                .proformaCreatedAt(entity.getProformaCreatedAt())
                .proformaUpdatedAt(entity.getProformaUpdatedAt())
                .build();
    }

    private BigDecimal defaultIfNull(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private String blankToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}