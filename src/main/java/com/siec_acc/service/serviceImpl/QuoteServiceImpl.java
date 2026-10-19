package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.QuoteItemRequestDto;
import com.siec_acc.dto.request.QuoteRequestDto;
import com.siec_acc.dto.response.*;
import com.siec_acc.entity.QuoteEntity;
import com.siec_acc.entity.QuoteItemEntity;
import com.siec_acc.enum_status.QuoteGstType;
import com.siec_acc.enum_status.QuoteStatus;
import com.siec_acc.exceptions.*;
import com.siec_acc.repository.QuoteRepository;
import com.siec_acc.service.QuoteService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteServiceImpl implements QuoteService {

    private static final Logger logger = LoggerFactory.getLogger(QuoteServiceImpl.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    // Manual status moves via PATCH. (Edit form can still set status directly, like invoice.)
    private static final Map<QuoteStatus, Set<QuoteStatus>> ALLOWED_MOVES = Map.of(
            QuoteStatus.DRAFT, EnumSet.of(QuoteStatus.SENT),
            QuoteStatus.SENT, EnumSet.of(QuoteStatus.ACCEPTED, QuoteStatus.DECLINED));

    private final QuoteRepository quoteRepository;

    public QuoteServiceImpl(QuoteRepository quoteRepository) {
        this.quoteRepository = quoteRepository;
    }

    // ---------------- CREATE ----------------
    @Override
    @Transactional
    public QuoteResponseDto createQuote(QuoteRequestDto dto, byte[] fileBytes, String contentType, String fileName) {
        validate(dto);

        String quoteStrId = generateNextQuoteNumber();
        QuoteEntity entity = QuoteEntity.builder()
                .quoteStrId(quoteStrId)
                .quotePdfUrl("/api/v1/quotes/" + quoteStrId)
                .quoteConvertedToSo(false)
                .build();

        applyRequest(entity, dto);

        if (fileBytes != null && fileBytes.length > 0) {
            setPdf(entity, fileBytes, contentType, fileName);
        }

        QuoteEntity saved = quoteRepository.save(entity);
        logger.info("Quote {} created (quotePrimeId={})", saved.getQuoteStrId(), saved.getQuotePrimeId());
        return toResponseDto(saved);
    }

    // ---------------- UPDATE ----------------
    @Override
    @Transactional
    public QuoteResponseDto updateQuote(Long quotePrimeId, QuoteRequestDto dto) {
        QuoteEntity entity = findEntityOrThrow(quotePrimeId);
        if (Boolean.TRUE.equals(entity.getQuoteConvertedToSo())) {
            throw new InvalidQuoteException("Quotation " + entity.getQuoteStrId() + " is already converted to a Sales Order and can't be edited.");
        }
        validate(dto);
        applyRequest(entity, dto);

        QuoteEntity saved = quoteRepository.save(entity);
        logger.info("Quote {} updated (quotePrimeId={})", saved.getQuoteStrId(), saved.getQuotePrimeId());
        return toResponseDto(saved);
    }

    // ---------------- READ ----------------
    @Override
    @Transactional
    public QuoteResponseDto getQuoteById(Long quotePrimeId) {
        expireOverdueQuotes();
        return toResponseDto(findEntityOrThrow(quotePrimeId));
    }

    @Override
    @Transactional
    public PagedResponseDto<QuoteResponseDto> getAllQuotes(QuoteStatus status, Long clientPrimeId, String search,
                                                           int pageNumber, int pageSize) {
        expireOverdueQuotes();

        int safePage = Math.max(pageNumber, 1) - 1;
        int safeSize = pageSize <= 0 ? 25 : pageSize;

        Page<QuoteEntity> page = quoteRepository.search(status, clientPrimeId, blankToNull(search),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "quoteCreatedAt")));

        return PagedResponseDto.<QuoteResponseDto>builder()
                .content(page.getContent().stream().map(this::toResponseDto).collect(Collectors.toList()))
                .pageNumber(pageNumber)
                .pageSize(safeSize)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    // ---------------- DELETE ----------------
    @Override
    @Transactional
    public void deleteQuote(Long quotePrimeId) {
        QuoteEntity entity = findEntityOrThrow(quotePrimeId);
        quoteRepository.delete(entity);
        logger.info("Quote {} deleted (quotePrimeId={})", entity.getQuoteStrId(), quotePrimeId);
    }

    // ---------------- STATS ----------------
    @Override
    @Transactional
    public QuoteStatsResponseDto getQuoteStats() {
        expireOverdueQuotes();
        return QuoteStatsResponseDto.builder()
                .totalCount(quoteRepository.count())
                .draftCount(quoteRepository.countByQuoteStatus(QuoteStatus.DRAFT))
                .sentCount(quoteRepository.countByQuoteStatus(QuoteStatus.SENT))
                .acceptedCount(quoteRepository.countByQuoteStatus(QuoteStatus.ACCEPTED))
                .totalValueInr(quoteRepository.sumTotalValueInr())
                .build();
    }

    // ---------------- ACTIONS ----------------
    @Override
    @Transactional
    public QuoteResponseDto updateQuoteStatus(Long quotePrimeId, QuoteStatus status) {
        expireOverdueQuotes();
        QuoteEntity entity = findEntityOrThrow(quotePrimeId);

        if (status == null || !ALLOWED_MOVES.getOrDefault(entity.getQuoteStatus(), Set.of()).contains(status)) {
            throw new InvalidQuoteException("Quotation " + entity.getQuoteStrId() + " can't move from "
                    + entity.getQuoteStatus() + " to " + status + ".");
        }
        entity.setQuoteStatus(status);
        logger.info("Quote {} status -> {}", entity.getQuoteStrId(), status);
        return toResponseDto(quoteRepository.save(entity));
    }

    // No Sales Order entity yet: only flags the quote. Hook SO creation in here later.
    @Override
    @Transactional
    public QuoteResponseDto convertToSalesOrder(Long quotePrimeId) {
        QuoteEntity entity = findEntityOrThrow(quotePrimeId);
        if (entity.getQuoteStatus() != QuoteStatus.ACCEPTED || Boolean.TRUE.equals(entity.getQuoteConvertedToSo())) {
            throw new InvalidQuoteException("Only an accepted quotation that isn't already converted can be converted to a Sales Order.");
        }
        entity.setQuoteConvertedToSo(true);
        logger.info("Quote {} flagged as converted to SO", entity.getQuoteStrId());
        return toResponseDto(quoteRepository.save(entity));
    }

    // ---------------- PDF ----------------
    @Override
    @Transactional
    public QuoteResponseDto uploadQuotePdf(Long quotePrimeId, byte[] fileBytes, String contentType, String fileName) {
        QuoteEntity entity = findEntityOrThrow(quotePrimeId);
        if (fileBytes == null || fileBytes.length == 0) {
            throw new FileProcessingException("The uploaded PDF appears to be empty. Please choose a valid file.");
        }
        setPdf(entity, fileBytes, contentType, fileName);
        return toResponseDto(quoteRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadQuotePdf(String quoteStrId) {
        QuoteEntity entity = quoteRepository.findByQuoteStrId(quoteStrId)
                .orElseThrow(() -> new QuoteNotFoundException("We couldn't find a quotation with number " + quoteStrId + "."));

        if (entity.getQuotePdfBlob() == null) {
            throw new QuoteNotFoundException("No PDF has been generated yet for quotation " + quoteStrId + ".");
        }
        return FileDownloadDto.builder()
                .data(entity.getQuotePdfBlob())
                .contentType(entity.getQuotePdfContentType() != null ? entity.getQuotePdfContentType() : "application/pdf")
                .fileName(entity.getQuotePdfFileName() != null ? entity.getQuotePdfFileName() : quoteStrId + ".pdf")
                .build();
    }

    // ================= helpers =================

    private void setPdf(QuoteEntity entity, byte[] bytes, String contentType, String fileName) {
        entity.setQuotePdfBlob(bytes);
        entity.setQuotePdfContentType(contentType != null ? contentType : "application/pdf");
        entity.setQuotePdfFileName(fileName != null ? fileName : entity.getQuoteStrId() + ".pdf");
        entity.setQuotePdfUrl("/api/v1/quotes/" + entity.getQuoteStrId());
    }

    private void expireOverdueQuotes() {
        int n = quoteRepository.expireOverdue(QuoteStatus.SENT, QuoteStatus.EXPIRED, LocalDate.now(), LocalDateTime.now());
        if (n > 0) logger.info("{} sent quotation(s) auto-expired", n);
    }

    private QuoteEntity findEntityOrThrow(Long quotePrimeId) {
        return quoteRepository.findById(quotePrimeId)
                .orElseThrow(() -> new QuoteNotFoundException("We couldn't find that quotation. It may have been deleted."));
    }

    private void validate(QuoteRequestDto dto) {
        if (dto.getQuoteItems() == null || dto.getQuoteItems().isEmpty()) {
            throw new QuoteItemRequiredException("Please add at least one item to the quotation before saving.");
        }
        for (QuoteItemRequestDto i : dto.getQuoteItems()) {
            if (i.getQuoteItemQty() == null || i.getQuoteItemQty().compareTo(BigDecimal.ZERO) <= 0
                    || i.getQuoteItemRate() == null || i.getQuoteItemRate().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidQuoteException("Every item needs a quantity greater than 0 and a valid rate.");
            }
        }
        if (dto.getQuoteDate() == null || dto.getQuoteValidTill() == null || dto.getQuoteClientPrimeId() == null) {
            throw new InvalidQuoteException("Quotation date, valid-till date and client are required.");
        }
        if (dto.getQuoteValidTill().isBefore(dto.getQuoteDate())) {
            throw new InvalidQuoteException("Valid-till date can't be before the quotation date.");
        }
    }

    private void applyRequest(QuoteEntity e, QuoteRequestDto d) {
        e.setQuoteDate(d.getQuoteDate());
        e.setQuoteValidTill(d.getQuoteValidTill());

        e.setQuoteClientPrimeId(d.getQuoteClientPrimeId());
        e.setQuoteClientName(d.getQuoteClientName());
        e.setQuoteClientCompanyName(d.getQuoteClientCompanyName());
        e.setQuoteClientGstin(d.getQuoteClientGstin());
        e.setQuoteClientEmail(d.getQuoteClientEmail());
        e.setQuoteClientState(d.getQuoteClientState());
        e.setQuoteClientStateCode(d.getQuoteClientStateCode());
        e.setQuoteClientCountry(d.getQuoteClientCountry());
        e.setQuoteClientBillingAddress(d.getQuoteClientBillingAddress());
        e.setQuoteShippingAddress(d.getQuoteShippingAddress());

        e.setQuoteCompanyStateCode(d.getQuoteCompanyStateCode());
        e.setQuotePlaceOfSupply(d.getQuotePlaceOfSupply());
        e.setQuotePlaceOfSupplyStateCode(d.getQuotePlaceOfSupplyStateCode());
        e.setQuoteGstType(resolveGstType(d.getQuoteCompanyStateCode(), d.getQuotePlaceOfSupplyStateCode()));

        e.setQuoteCurrency(d.getQuoteCurrency());
        e.setQuoteExchangeRate(orDefault(d.getQuoteExchangeRate(), BigDecimal.ONE));
        e.setQuoteDiscountPercent(orDefault(d.getQuoteDiscountPercent(), BigDecimal.ZERO));
        e.setQuoteShippingCharges(orDefault(d.getQuoteShippingCharges(), BigDecimal.ZERO));
        e.setQuoteOtherCharges(orDefault(d.getQuoteOtherCharges(), BigDecimal.ZERO));
        e.setQuoteRoundOff(orDefault(d.getQuoteRoundOff(), BigDecimal.ZERO));

        if (d.getQuoteStatus() != null) {
            e.setQuoteStatus(d.getQuoteStatus());
        } else if (e.getQuoteStatus() == null) {
            e.setQuoteStatus(QuoteStatus.DRAFT);
        }
        e.setQuoteReferenceNumber(d.getQuoteReferenceNumber());
        e.setQuoteCustomerNotes(d.getQuoteCustomerNotes());
        e.setQuoteTerms(d.getQuoteTerms());
        e.setQuoteTemplateId(d.getQuoteTemplateId());

        e.getQuoteItems().clear();
        buildItemsAndTotals(e, d.getQuoteItems());
    }

    // Place of supply state vs company state (Zoho behaviour): same -> CGST+SGST, different -> IGST.
    private QuoteGstType resolveGstType(String companyStateCode, String placeOfSupplyStateCode) {
        if (isBlank(companyStateCode) || isBlank(placeOfSupplyStateCode)) {
            throw new InvalidQuoteException("Company state code and place of supply state code are required to work out the GST type.");
        }
        return companyStateCode.trim().equals(placeOfSupplyStateCode.trim())
                ? QuoteGstType.CGST_SGST : QuoteGstType.IGST;
    }

    /**
     * Per item: amount = qty*rate; taxable = amount - header discount share;
     * tax = taxable * itemGst%; split CGST/SGST (half each) or IGST.
     * Header: total = taxable + taxes + shipping + other + roundOff.
     */
    private void buildItemsAndTotals(QuoteEntity e, List<QuoteItemRequestDto> dtos) {
        boolean intraState = e.getQuoteGstType() == QuoteGstType.CGST_SGST;

        BigDecimal subTotal = BigDecimal.ZERO, taxableTotal = BigDecimal.ZERO;
        BigDecimal cgstTotal = BigDecimal.ZERO, sgstTotal = BigDecimal.ZERO, igstTotal = BigDecimal.ZERO;

        for (QuoteItemRequestDto d : dtos) {
            BigDecimal amount = d.getQuoteItemQty().multiply(d.getQuoteItemRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discountShare = amount.multiply(e.getQuoteDiscountPercent()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            BigDecimal taxable = amount.subtract(discountShare);

            BigDecimal gstPercent = orDefault(d.getQuoteItemGstPercent(), BigDecimal.ZERO);
            BigDecimal tax = taxable.multiply(gstPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);

            BigDecimal cgst = BigDecimal.ZERO, sgst = BigDecimal.ZERO, igst = BigDecimal.ZERO;
            if (intraState) {
                cgst = tax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                sgst = tax.subtract(cgst);
            } else {
                igst = tax;
            }

            e.getQuoteItems().add(QuoteItemEntity.builder()
                    .quote(e)
                    .quoteItemProductId(d.getQuoteItemProductId())
                    .quoteItemVariantId(d.getQuoteItemVariantId())
                    .quoteItemName(d.getQuoteItemName())
                    .quoteItemDescription(d.getQuoteItemDescription())
                    .quoteItemHsnCode(d.getQuoteItemHsnCode())
                    .quoteItemUnit(d.getQuoteItemUnit())
                    .quoteItemQty(d.getQuoteItemQty())
                    .quoteItemRate(d.getQuoteItemRate())
                    .quoteItemAmount(amount)
                    .quoteItemTaxableAmount(taxable)
                    .quoteItemGstPercent(gstPercent)
                    .quoteItemCgstAmount(cgst)
                    .quoteItemSgstAmount(sgst)
                    .quoteItemIgstAmount(igst)
                    .build());

            subTotal = subTotal.add(amount);
            taxableTotal = taxableTotal.add(taxable);
            cgstTotal = cgstTotal.add(cgst);
            sgstTotal = sgstTotal.add(sgst);
            igstTotal = igstTotal.add(igst);
        }

        BigDecimal total = taxableTotal.add(cgstTotal).add(sgstTotal).add(igstTotal)
                .add(e.getQuoteShippingCharges())
                .add(e.getQuoteOtherCharges())
                .add(e.getQuoteRoundOff());

        e.setQuoteSubTotal(subTotal);
        e.setQuoteDiscountAmount(subTotal.subtract(taxableTotal));
        e.setQuoteTaxableAmount(taxableTotal);
        e.setQuoteCgstAmount(cgstTotal);
        e.setQuoteSgstAmount(sgstTotal);
        e.setQuoteIgstAmount(igstTotal);
        e.setQuoteTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        e.setQuoteTotalAmountInr(total.multiply(e.getQuoteExchangeRate()).setScale(2, RoundingMode.HALF_UP));
    }

    // "QUO-25-26-001" — running number resets each financial year (Apr-Mar).
    private String generateNextQuoteNumber() {
        LocalDate today = LocalDate.now();
        int fyStart = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        String prefix = String.format("QUO-%02d-%02d-", fyStart % 100, (fyStart + 1) % 100);
        Integer max = quoteRepository.findMaxSequence(prefix, prefix.length() + 1);
        return prefix + String.format("%03d", (max == null ? 0 : max) + 1);
    }

    private QuoteResponseDto toResponseDto(QuoteEntity e) {
        List<QuoteItemResponseDto> items = e.getQuoteItems().stream()
                .map(i -> QuoteItemResponseDto.builder()
                        .quoteItemPrimeId(i.getQuoteItemPrimeId())
                        .quoteItemProductId(i.getQuoteItemProductId())
                        .quoteItemVariantId(i.getQuoteItemVariantId())
                        .quoteItemName(i.getQuoteItemName())
                        .quoteItemDescription(i.getQuoteItemDescription())
                        .quoteItemHsnCode(i.getQuoteItemHsnCode())
                        .quoteItemUnit(i.getQuoteItemUnit())
                        .quoteItemQty(i.getQuoteItemQty())
                        .quoteItemRate(i.getQuoteItemRate())
                        .quoteItemAmount(i.getQuoteItemAmount())
                        .quoteItemTaxableAmount(i.getQuoteItemTaxableAmount())
                        .quoteItemGstPercent(i.getQuoteItemGstPercent())
                        .quoteItemCgstAmount(i.getQuoteItemCgstAmount())
                        .quoteItemSgstAmount(i.getQuoteItemSgstAmount())
                        .quoteItemIgstAmount(i.getQuoteItemIgstAmount())
                        .build())
                .collect(Collectors.toList());

        return QuoteResponseDto.builder()
                .quotePrimeId(e.getQuotePrimeId())
                .quoteStrId(e.getQuoteStrId())
                .quoteDate(e.getQuoteDate())
                .quoteValidTill(e.getQuoteValidTill())
                .quoteClientPrimeId(e.getQuoteClientPrimeId())
                .quoteClientName(e.getQuoteClientName())
                .quoteClientCompanyName(e.getQuoteClientCompanyName())
                .quoteClientGstin(e.getQuoteClientGstin())
                .quoteClientEmail(e.getQuoteClientEmail())
                .quoteClientState(e.getQuoteClientState())
                .quoteClientStateCode(e.getQuoteClientStateCode())
                .quoteClientCountry(e.getQuoteClientCountry())
                .quoteClientBillingAddress(e.getQuoteClientBillingAddress())
                .quoteShippingAddress(e.getQuoteShippingAddress())
                .quoteCompanyStateCode(e.getQuoteCompanyStateCode())
                .quotePlaceOfSupply(e.getQuotePlaceOfSupply())
                .quotePlaceOfSupplyStateCode(e.getQuotePlaceOfSupplyStateCode())
                .quoteGstType(e.getQuoteGstType())
                .quoteCurrency(e.getQuoteCurrency())
                .quoteExchangeRate(e.getQuoteExchangeRate())
                .quoteSubTotal(e.getQuoteSubTotal())
                .quoteDiscountPercent(e.getQuoteDiscountPercent())
                .quoteDiscountAmount(e.getQuoteDiscountAmount())
                .quoteTaxableAmount(e.getQuoteTaxableAmount())
                .quoteCgstAmount(e.getQuoteCgstAmount())
                .quoteSgstAmount(e.getQuoteSgstAmount())
                .quoteIgstAmount(e.getQuoteIgstAmount())
                .quoteShippingCharges(e.getQuoteShippingCharges())
                .quoteOtherCharges(e.getQuoteOtherCharges())
                .quoteRoundOff(e.getQuoteRoundOff())
                .quoteTotalAmount(e.getQuoteTotalAmount())
                .quoteTotalAmountInr(e.getQuoteTotalAmountInr())
                .quoteStatus(e.getQuoteStatus())
                .quoteReferenceNumber(e.getQuoteReferenceNumber())
                .quoteCustomerNotes(e.getQuoteCustomerNotes())
                .quoteTerms(e.getQuoteTerms())
                .quoteTemplateId(e.getQuoteTemplateId())
                .quoteConvertedToSo(e.getQuoteConvertedToSo())
                .quotePdfUrl(e.getQuotePdfUrl())
                .quoteItems(items)
                .quoteCreatedAt(e.getQuoteCreatedAt())
                .quoteUpdatedAt(e.getQuoteUpdatedAt())
                .build();
    }

    private BigDecimal orDefault(BigDecimal v, BigDecimal fallback) { return v != null ? v : fallback; }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private String blankToNull(String s) { return isBlank(s) ? null : s.trim(); }


    @Override
    @Transactional
    public PagedResponseDto<QuoteSummaryResponseDto> getQuoteSummaries(int pageNumber, int pageSize) {
        expireOverdueQuotes(); // keeps quoteStatus accurate (SENT -> EXPIRED by valid-till)

        int safePage = Math.max(pageNumber, 1) - 1; // 1-based like invoice
        int safeSize = pageSize <= 0 ? 25 : pageSize;

        Page<QuoteSummaryResponseDto> page = quoteRepository.findQuoteSummaries(
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "quotePrimeId")));

        return PagedResponseDto.<QuoteSummaryResponseDto>builder()
                .content(page.getContent())
                .pageNumber(pageNumber)
                .pageSize(safeSize)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}