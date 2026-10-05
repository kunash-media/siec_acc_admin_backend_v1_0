package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.InvoiceItemRequestDto;
import com.siec_acc.dto.request.InvoiceRequestDto;
import com.siec_acc.dto.response.FileDownloadDto;
import com.siec_acc.dto.response.InvoiceItemResponseDto;
import com.siec_acc.dto.response.InvoiceResponseDto;
import com.siec_acc.dto.response.InvoiceStatsResponseDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.entity.InvoiceEntity;
import com.siec_acc.entity.InvoiceItemEntity;
import com.siec_acc.enum_status.InvoiceStatus;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.exceptions.InvalidInvoiceStatusException;
import com.siec_acc.exceptions.InvoiceItemRequiredException;
import com.siec_acc.exceptions.InvoiceNotFoundException;
import com.siec_acc.repository.InvoiceRepository;
import com.siec_acc.service.InvoiceService;
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
public class InvoiceServiceImpl implements InvoiceService {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);

    private final InvoiceRepository invoiceRepository;

    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // ---------------------------------------------------------------
    // CREATE (plain — no PDF; used internally e.g. by proforma conversion)
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceRequestDto requestDto) {
        if (requestDto.getInvoiceItems() == null || requestDto.getInvoiceItems().isEmpty()) {
            logger.warn("Create invoice rejected — no line items provided (customerId={})", requestDto.getInvoiceCustomerId());
            throw new InvoiceItemRequiredException("Please add at least one item to the invoice before saving.");
        }

        String invoiceStrId = generateNextInvoiceNumber();
        logger.info("Creating invoice {} for customerId={}", invoiceStrId, requestDto.getInvoiceCustomerId());

        InvoiceEntity entity = InvoiceEntity.builder()
                .invoiceStrId(invoiceStrId)
                // Download URL for the stored PDF — GET {baseUrl}/api/v1/invoices/{invoiceStrId}
                .invoicePdfUrl("/api/v1/invoices/" + invoiceStrId)
                .invoiceDate(requestDto.getInvoiceDate())
                .invoiceDueDate(requestDto.getInvoiceDueDate())
                .invoiceCustomerId(requestDto.getInvoiceCustomerId())
                .invoiceCustomerName(requestDto.getInvoiceCustomerName())
                .invoiceCustomerGst(requestDto.getInvoiceCustomerGst())
                .invoiceCustomerEmail(requestDto.getInvoiceCustomerEmail())
                .invoiceCustomerState(requestDto.getInvoiceCustomerState())
                .invoiceCustomerStateCode(requestDto.getInvoiceCustomerStateCode())
                .invoiceCustomerAddress(requestDto.getInvoiceCustomerAddress())
                .invoiceShippingAddress(requestDto.getInvoiceShippingAddress())
                .invoiceCurrency(requestDto.getInvoiceCurrency())
                .invoiceExchangeRate(defaultIfNull(requestDto.getInvoiceExchangeRate(), BigDecimal.ONE))
                .invoiceDiscountPercent(defaultIfNull(requestDto.getInvoiceDiscountPercent(), BigDecimal.ZERO))
                .invoiceTaxPercent(defaultIfNull(requestDto.getInvoiceTaxPercent(), BigDecimal.ZERO))
                .invoiceShippingCharges(defaultIfNull(requestDto.getInvoiceShippingCharges(), BigDecimal.ZERO))
                .invoiceOtherCharges(defaultIfNull(requestDto.getInvoiceOtherCharges(), BigDecimal.ZERO))
                .invoiceRoundOff(defaultIfNull(requestDto.getInvoiceRoundOff(), BigDecimal.ZERO))
                .invoicePaidAmount(defaultIfNull(requestDto.getInvoicePaidAmount(), BigDecimal.ZERO))
                .invoiceStatus(requestDto.getInvoiceStatus() != null ? requestDto.getInvoiceStatus() : InvoiceStatus.UNPAID)
                .invoicePlaceOfSupply(requestDto.getInvoicePlaceOfSupply())
                .invoiceTerms(requestDto.getInvoiceTerms())
                .invoiceNotes(requestDto.getInvoiceNotes())
                .invoiceReferenceNumber(requestDto.getInvoiceReferenceNumber())
                .invoicePoNumber(requestDto.getInvoicePoNumber())
                .invoiceEwayBillNumber(requestDto.getInvoiceEwayBillNumber())
                .invoicePaymentMode(requestDto.getInvoicePaymentMode())
                .invoiceAttachmentUrl(requestDto.getInvoiceAttachmentUrl())
                .invoiceSignatureUrl(requestDto.getInvoiceSignatureUrl())
                .invoiceAuthorizedSignatory(requestDto.getInvoiceAuthorizedSignatory())
                .invoiceIsRecurring(defaultIfNull(requestDto.getInvoiceIsRecurring(), Boolean.FALSE))
                .invoiceRecurringFrequency(requestDto.getInvoiceRecurringFrequency())
                .invoiceConvertedFromProformaId(requestDto.getInvoiceConvertedFromProformaId())
                .invoiceTemplateId(requestDto.getInvoiceTemplateId())
                .build();

        attachItems(entity, requestDto.getInvoiceItems());
        applyTotals(entity);

        InvoiceEntity saved = invoiceRepository.save(entity);
        logger.info("Invoice {} created successfully (invoicePrimeId={})", saved.getInvoiceStrId(), saved.getInvoicePrimeId());
        return toResponseDto(saved);
    }

    // ---------------------------------------------------------------
    // CREATE + PDF  (backs the frontend form submit: JSON + file together)
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public InvoiceResponseDto createInvoice(InvoiceRequestDto requestDto, byte[] fileBytes, String contentType, String fileName) {
        InvoiceResponseDto created = createInvoice(requestDto);

        if (fileBytes != null && fileBytes.length > 0) {
            logger.info("Attaching PDF ({} bytes) to newly created invoice {}", fileBytes.length, created.getInvoiceStrId());
            created = uploadInvoicePdf(created.getInvoicePrimeId(), fileBytes, contentType, fileName);
        } else {
            logger.info("No PDF attached at creation time for invoice {} — can be uploaded later via upload-pdf", created.getInvoiceStrId());
        }

        return created;
    }

    // ---------------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public InvoiceResponseDto updateInvoice(Long invoicePrimeId, InvoiceRequestDto requestDto) {
        InvoiceEntity entity = findEntityOrThrow(invoicePrimeId);

        if (requestDto.getInvoiceItems() == null || requestDto.getInvoiceItems().isEmpty()) {
            logger.warn("Update invoice rejected — no line items provided (invoicePrimeId={})", invoicePrimeId);
            throw new InvoiceItemRequiredException("Please add at least one item to the invoice before saving.");
        }

        entity.setInvoiceDate(requestDto.getInvoiceDate());
        entity.setInvoiceDueDate(requestDto.getInvoiceDueDate());
        entity.setInvoiceCustomerId(requestDto.getInvoiceCustomerId());
        entity.setInvoiceCustomerName(requestDto.getInvoiceCustomerName());
        entity.setInvoiceCustomerGst(requestDto.getInvoiceCustomerGst());
        entity.setInvoiceCustomerEmail(requestDto.getInvoiceCustomerEmail());
        entity.setInvoiceCustomerState(requestDto.getInvoiceCustomerState());
        entity.setInvoiceCustomerStateCode(requestDto.getInvoiceCustomerStateCode());
        entity.setInvoiceCustomerAddress(requestDto.getInvoiceCustomerAddress());
        entity.setInvoiceShippingAddress(requestDto.getInvoiceShippingAddress());
        entity.setInvoiceCurrency(requestDto.getInvoiceCurrency());
        entity.setInvoiceExchangeRate(defaultIfNull(requestDto.getInvoiceExchangeRate(), BigDecimal.ONE));
        entity.setInvoiceDiscountPercent(defaultIfNull(requestDto.getInvoiceDiscountPercent(), BigDecimal.ZERO));
        entity.setInvoiceTaxPercent(defaultIfNull(requestDto.getInvoiceTaxPercent(), BigDecimal.ZERO));
        entity.setInvoiceShippingCharges(defaultIfNull(requestDto.getInvoiceShippingCharges(), BigDecimal.ZERO));
        entity.setInvoiceOtherCharges(defaultIfNull(requestDto.getInvoiceOtherCharges(), BigDecimal.ZERO));
        entity.setInvoiceRoundOff(defaultIfNull(requestDto.getInvoiceRoundOff(), BigDecimal.ZERO));
        entity.setInvoicePaidAmount(defaultIfNull(requestDto.getInvoicePaidAmount(), entity.getInvoicePaidAmount()));
        if (requestDto.getInvoiceStatus() != null) {
            entity.setInvoiceStatus(requestDto.getInvoiceStatus());
        }
        entity.setInvoicePlaceOfSupply(requestDto.getInvoicePlaceOfSupply());
        entity.setInvoiceTerms(requestDto.getInvoiceTerms());
        entity.setInvoiceNotes(requestDto.getInvoiceNotes());
        entity.setInvoiceReferenceNumber(requestDto.getInvoiceReferenceNumber());
        entity.setInvoicePoNumber(requestDto.getInvoicePoNumber());
        entity.setInvoiceEwayBillNumber(requestDto.getInvoiceEwayBillNumber());
        entity.setInvoicePaymentMode(requestDto.getInvoicePaymentMode());
        entity.setInvoiceAttachmentUrl(requestDto.getInvoiceAttachmentUrl());
        entity.setInvoiceSignatureUrl(requestDto.getInvoiceSignatureUrl());
        entity.setInvoiceAuthorizedSignatory(requestDto.getInvoiceAuthorizedSignatory());
        entity.setInvoiceIsRecurring(defaultIfNull(requestDto.getInvoiceIsRecurring(), entity.getInvoiceIsRecurring()));
        entity.setInvoiceRecurringFrequency(requestDto.getInvoiceRecurringFrequency());
        entity.setInvoiceTemplateId(requestDto.getInvoiceTemplateId());

        entity.getInvoiceItems().clear();
        attachItems(entity, requestDto.getInvoiceItems());
        applyTotals(entity);

        InvoiceEntity saved = invoiceRepository.save(entity);
        logger.info("Invoice {} updated successfully (invoicePrimeId={})", saved.getInvoiceStrId(), saved.getInvoicePrimeId());
        return toResponseDto(saved);
    }

    // ---------------------------------------------------------------
    // READ
    // ---------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoiceById(Long invoicePrimeId) {
        return toResponseDto(findEntityOrThrow(invoicePrimeId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<InvoiceResponseDto> getAllInvoices(InvoiceStatus status,
                                                               Long customerId,
                                                               String search,
                                                               int pageNumber,
                                                               int pageSize) {
        int safePage = Math.max(pageNumber, 1) - 1; // frontend is 1-based
        int safeSize = pageSize <= 0 ? 25 : pageSize;

        Page<InvoiceEntity> page = invoiceRepository.search(
                status, customerId, blankToNull(search),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "invoiceCreatedAt")));

        List<InvoiceResponseDto> content = page.getContent().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        return PagedResponseDto.<InvoiceResponseDto>builder()
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
    public void deleteInvoice(Long invoicePrimeId) {
        InvoiceEntity entity = findEntityOrThrow(invoicePrimeId);
        invoiceRepository.delete(entity);
        logger.info("Invoice {} deleted (invoicePrimeId={})", entity.getInvoiceStrId(), invoicePrimeId);
    }

    // ---------------------------------------------------------------
    // STATS  (backs the Total / Due / Overdue / Total Value stat cards)
    // ---------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public InvoiceStatsResponseDto getInvoiceStats() {
        long total = invoiceRepository.count();
        long due = invoiceRepository.countByInvoiceStatus(InvoiceStatus.UNPAID)
                + invoiceRepository.countByInvoiceStatus(InvoiceStatus.PARTIAL);
        long overdue = invoiceRepository.countByInvoiceStatusAndInvoiceDueDateBefore(InvoiceStatus.UNPAID, LocalDate.now());
        BigDecimal totalValue = invoiceRepository.sumTotalValueInr();

        return InvoiceStatsResponseDto.builder()
                .totalCount(total)
                .dueCount(due)
                .overdueCount(overdue)
                .totalValueInr(totalValue)
                .build();
    }

    // ---------------------------------------------------------------
    // ACTIONS
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public InvoiceResponseDto markAsSent(Long invoicePrimeId) {
        InvoiceEntity entity = findEntityOrThrow(invoicePrimeId);
        if (entity.getInvoiceStatus() != InvoiceStatus.UNPAID
                && entity.getInvoiceStatus() != InvoiceStatus.PARTIAL
                && entity.getInvoiceStatus() != InvoiceStatus.OVERDUE) {
            logger.warn("Rejected mark-sent for invoice {} — invalid current status {}", entity.getInvoiceStrId(), entity.getInvoiceStatus());
            throw new InvalidInvoiceStatusException(
                    "Invoice " + entity.getInvoiceStrId() + " cannot be sent from its current status (" + entity.getInvoiceStatus() + ").");
        }
        logger.info("Invoice {} marked as sent", entity.getInvoiceStrId());
        return toResponseDto(invoiceRepository.save(entity));
    }

    @Override
    @Transactional
    public InvoiceResponseDto recordPayment(Long invoicePrimeId, BigDecimal amount) {
        InvoiceEntity entity = findEntityOrThrow(invoicePrimeId);
        BigDecimal balance = entity.getInvoiceTotalAmount().subtract(entity.getInvoicePaidAmount());

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Rejected payment for invoice {} — invalid amount {}", entity.getInvoiceStrId(), amount);
            throw new InvalidInvoiceStatusException("Payment amount must be greater than 0.");
        }
        if (amount.compareTo(balance) > 0) {
            logger.warn("Rejected payment for invoice {} — amount {} exceeds balance due {}", entity.getInvoiceStrId(), amount, balance);
            throw new InvalidInvoiceStatusException("Amount exceeds the balance due (" + balance + ").");
        }

        entity.setInvoicePaidAmount(entity.getInvoicePaidAmount().add(amount));
        if (entity.getInvoicePaidAmount().compareTo(entity.getInvoiceTotalAmount()) >= 0) {
            entity.setInvoiceStatus(InvoiceStatus.PAID);
        } else if (entity.getInvoicePaidAmount().compareTo(BigDecimal.ZERO) > 0) {
            entity.setInvoiceStatus(InvoiceStatus.PARTIAL);
        }

        logger.info("Payment of {} recorded for invoice {} (new status={})", amount, entity.getInvoiceStrId(), entity.getInvoiceStatus());
        return toResponseDto(invoiceRepository.save(entity));
    }

    // ---------------------------------------------------------------
    // PDF STORAGE  (LONGBLOB) — served back via GET /api/v1/invoices/{invoiceStrId}
    // ---------------------------------------------------------------
    @Override
    @Transactional
    public InvoiceResponseDto uploadInvoicePdf(Long invoicePrimeId, byte[] fileBytes, String contentType, String fileName) {
        InvoiceEntity entity = findEntityOrThrow(invoicePrimeId);

        if (fileBytes == null || fileBytes.length == 0) {
            logger.warn("Rejected PDF upload for invoice {} — empty file", entity.getInvoiceStrId());
            throw new FileProcessingException("The uploaded PDF appears to be empty. Please choose a valid file.");
        }

        entity.setInvoicePdfBlob(fileBytes);
        entity.setInvoicePdfContentType(contentType != null ? contentType : "application/pdf");
        entity.setInvoicePdfFileName(fileName != null ? fileName : entity.getInvoiceStrId() + ".pdf");
        entity.setInvoicePdfUrl(buildRelativeUrl(entity.getInvoiceStrId()));

        InvoiceEntity saved = invoiceRepository.save(entity);
        logger.info("PDF stored for invoice {} ({} bytes)", saved.getInvoiceStrId(), fileBytes.length);
        return toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadInvoicePdf(String invoiceStrId) {
        InvoiceEntity entity = invoiceRepository.findByInvoiceStrId(invoiceStrId)
                .orElseThrow(() -> {
                    logger.warn("PDF download requested for unknown invoice number {}", invoiceStrId);
                    return new InvoiceNotFoundException("We couldn't find an invoice with number " + invoiceStrId + ".");
                });

        if (entity.getInvoicePdfBlob() == null) {
            logger.warn("PDF download requested but no file stored yet for invoice {}", invoiceStrId);
            throw new InvoiceNotFoundException("No PDF has been generated yet for invoice " + invoiceStrId + ".");
        }

        return FileDownloadDto.builder()
                .data(entity.getInvoicePdfBlob())
                .contentType(entity.getInvoicePdfContentType() != null ? entity.getInvoicePdfContentType() : "application/pdf")
                .fileName(entity.getInvoicePdfFileName() != null ? entity.getInvoicePdfFileName() : invoiceStrId + ".pdf")
                .build();
    }

    // Relative path only — frontend/consumer prepends its own base URL.
    // e.g. baseUrl + "/api/v1/invoices/INV-25-26-001" -> downloads the PDF.
    private String buildRelativeUrl(String invoiceStrId) {
        return "/api/v1/invoices/" + invoiceStrId;
    }

    private InvoiceEntity findEntityOrThrow(Long invoicePrimeId) {
        return invoiceRepository.findById(invoicePrimeId)
                .orElseThrow(() -> {
                    logger.warn("Invoice not found for invoicePrimeId={}", invoicePrimeId);
                    return new InvoiceNotFoundException("We couldn't find that invoice. It may have been deleted.");
                });
    }

    private void attachItems(InvoiceEntity entity, List<InvoiceItemRequestDto> itemDtos) {
        List<InvoiceItemEntity> items = new ArrayList<>();
        for (InvoiceItemRequestDto dto : itemDtos) {
            BigDecimal amount = dto.getInvoiceItemQty().multiply(dto.getInvoiceItemRate());
            InvoiceItemEntity item = InvoiceItemEntity.builder()
                    .invoice(entity)
                    .invoiceItemProductId(dto.getInvoiceItemProductId())
                    .invoiceItemName(dto.getInvoiceItemName())
                    .invoiceItemHsnCode(dto.getInvoiceItemHsnCode())
                    .invoiceItemUnit(dto.getInvoiceItemUnit())
                    .invoiceItemQty(dto.getInvoiceItemQty())
                    .invoiceItemRate(dto.getInvoiceItemRate())
                    .invoiceItemGstPercent(defaultIfNull(dto.getInvoiceItemGstPercent(), BigDecimal.ZERO))
                    .invoiceItemAmount(amount)
                    .build();
            items.add(item);
        }
        entity.getInvoiceItems().addAll(items);
    }

    /**
     * Mirrors the frontend's calcInvoiceTotal():
     * subtotal -> discount% -> +tax% -> +shipping +other (+/- roundOff) = total; total * exchangeRate = INR equivalent.
     */
    private void applyTotals(InvoiceEntity entity) {
        BigDecimal subtotal = entity.getInvoiceItems().stream()
                .map(InvoiceItemEntity::getInvoiceItemAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmt = subtotal.multiply(entity.getInvoiceDiscountPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discountAmt);

        BigDecimal taxAmt = afterDiscount.multiply(entity.getInvoiceTaxPercent())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal total = afterDiscount.add(taxAmt)
                .add(entity.getInvoiceShippingCharges())
                .add(entity.getInvoiceOtherCharges())
                .add(entity.getInvoiceRoundOff());

        entity.setInvoiceSubTotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        entity.setInvoiceTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        entity.setInvoiceTotalAmountInr(total.multiply(entity.getInvoiceExchangeRate()).setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * "INV-25-26-00X" style number, scoped to the current Indian financial year.
     * Swap this out for a DB sequence table if concurrent creation needs to be
     * strictly gap-free / race-safe.
     */
    private String generateNextInvoiceNumber() {
        int currentYear = Year.now().getValue();
        int fyStart = LocalDate.now().getMonthValue() >= 4 ? currentYear : currentYear - 1;
        String fyLabel = String.valueOf(fyStart).substring(2) + "-" + String.valueOf(fyStart + 1).substring(2);
        long countThisYear = invoiceRepository.count() + 1;
        return String.format("INV-%s-%03d", fyLabel, countThisYear);
    }

    private InvoiceResponseDto toResponseDto(InvoiceEntity entity) {
        List<InvoiceItemResponseDto> items = entity.getInvoiceItems().stream()
                .map(i -> InvoiceItemResponseDto.builder()
                        .invoiceItemPrimeId(i.getInvoiceItemPrimeId())
                        .invoiceItemProductId(i.getInvoiceItemProductId())
                        .invoiceItemName(i.getInvoiceItemName())
                        .invoiceItemHsnCode(i.getInvoiceItemHsnCode())
                        .invoiceItemUnit(i.getInvoiceItemUnit())
                        .invoiceItemQty(i.getInvoiceItemQty())
                        .invoiceItemRate(i.getInvoiceItemRate())
                        .invoiceItemGstPercent(i.getInvoiceItemGstPercent())
                        .invoiceItemAmount(i.getInvoiceItemAmount())
                        .build())
                .collect(Collectors.toList());

        BigDecimal balanceDue = entity.getInvoiceTotalAmount().subtract(
                defaultIfNull(entity.getInvoicePaidAmount(), BigDecimal.ZERO));

        return InvoiceResponseDto.builder()
                .invoicePrimeId(entity.getInvoicePrimeId())
                .invoiceStrId(entity.getInvoiceStrId())
                .invoiceDate(entity.getInvoiceDate())
                .invoiceDueDate(entity.getInvoiceDueDate())
                .invoiceCustomerId(entity.getInvoiceCustomerId())
                .invoiceCustomerName(entity.getInvoiceCustomerName())
                .invoiceCustomerGst(entity.getInvoiceCustomerGst())
                .invoiceCustomerEmail(entity.getInvoiceCustomerEmail())
                .invoiceCustomerState(entity.getInvoiceCustomerState())
                .invoiceCustomerStateCode(entity.getInvoiceCustomerStateCode())
                .invoiceCustomerAddress(entity.getInvoiceCustomerAddress())
                .invoiceShippingAddress(entity.getInvoiceShippingAddress())
                .invoiceCurrency(entity.getInvoiceCurrency())
                .invoiceExchangeRate(entity.getInvoiceExchangeRate())
                .invoiceSubTotal(entity.getInvoiceSubTotal())
                .invoiceDiscountPercent(entity.getInvoiceDiscountPercent())
                .invoiceTaxPercent(entity.getInvoiceTaxPercent())
                .invoiceShippingCharges(entity.getInvoiceShippingCharges())
                .invoiceOtherCharges(entity.getInvoiceOtherCharges())
                .invoiceRoundOff(entity.getInvoiceRoundOff())
                .invoiceTotalAmount(entity.getInvoiceTotalAmount())
                .invoiceTotalAmountInr(entity.getInvoiceTotalAmountInr())
                .invoicePaidAmount(entity.getInvoicePaidAmount())
                .invoiceBalanceDue(balanceDue)
                .invoiceStatus(entity.getInvoiceStatus())
                .invoicePlaceOfSupply(entity.getInvoicePlaceOfSupply())
                .invoiceTerms(entity.getInvoiceTerms())
                .invoiceNotes(entity.getInvoiceNotes())
                .invoiceReferenceNumber(entity.getInvoiceReferenceNumber())
                .invoicePoNumber(entity.getInvoicePoNumber())
                .invoiceEwayBillNumber(entity.getInvoiceEwayBillNumber())
                .invoicePaymentMode(entity.getInvoicePaymentMode())
                .invoicePdfUrl(entity.getInvoicePdfUrl())
                .invoiceAttachmentUrl(entity.getInvoiceAttachmentUrl())
                .invoiceSignatureUrl(entity.getInvoiceSignatureUrl())
                .invoiceAuthorizedSignatory(entity.getInvoiceAuthorizedSignatory())
                .invoiceIsRecurring(entity.getInvoiceIsRecurring())
                .invoiceRecurringFrequency(entity.getInvoiceRecurringFrequency())
                .invoiceConvertedFromProformaId(entity.getInvoiceConvertedFromProformaId())
                .invoiceTemplateId(entity.getInvoiceTemplateId())
                .invoiceItems(items)
                .invoiceCreatedAt(entity.getInvoiceCreatedAt())
                .invoiceUpdatedAt(entity.getInvoiceUpdatedAt())
                .build();
    }

    private BigDecimal defaultIfNull(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : fallback;
    }

    private Boolean defaultIfNull(Boolean value, Boolean fallback) {
        return value != null ? value : fallback;
    }

    private String blankToNull(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }
}