package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.DeliveryChallanItemRequestDto;
import com.siec_acc.dto.request.DeliveryChallanRequestDto;
import com.siec_acc.dto.request.InventoryStockUpdateDTO;
import com.siec_acc.dto.response.*;
import com.siec_acc.entity.DeliveryChallanEntity;
import com.siec_acc.entity.DeliveryChallanItemEntity;
import com.siec_acc.entity.WarehouseEntity;
import com.siec_acc.enum_status.DcStatus;
import com.siec_acc.enum_status.DcType;
import com.siec_acc.enum_status.QuoteGstType;
import com.siec_acc.exceptions.FileProcessingException;
import com.siec_acc.exceptions.InvalidOperationException;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.DeliveryChallanRepository;
import com.siec_acc.repository.ProductRepository;
import com.siec_acc.repository.VariantRepository;
import com.siec_acc.service.DeliveryChallanService;
import com.siec_acc.service.InventoryService;
import com.siec_acc.service.WarehouseService;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeliveryChallanServiceImpl implements DeliveryChallanService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryChallanServiceImpl.class);
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final String PDF_BASE = "/api/v1/delivery-challans/";
    private static final Map<DcStatus, DcStatus> NEXT = Map.of(
            DcStatus.DRAFT, DcStatus.DISPATCHED,
            DcStatus.DISPATCHED, DcStatus.DELIVERED);

    private final DeliveryChallanRepository dcRepository;
    private final InventoryService inventoryService;
    private final WarehouseService warehouseService;
    private final ProductRepository productRepository;
    private final VariantRepository variantRepository;

    public DeliveryChallanServiceImpl(DeliveryChallanRepository dcRepository, InventoryService inventoryService,
                                      WarehouseService warehouseService, ProductRepository productRepository,
                                      VariantRepository variantRepository) {
        this.dcRepository = dcRepository;
        this.inventoryService = inventoryService;
        this.warehouseService = warehouseService;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
    }

    // ---------------- CREATE ----------------
    @Override
    @Transactional
    public DeliveryChallanResponseDto createDc(DeliveryChallanRequestDto dto, byte[] fileBytes, String contentType, String fileName) {
        validate(dto);
        String dcStrId = generateNextDcNumber();
        DeliveryChallanEntity entity = DeliveryChallanEntity.builder()
                .dcStrId(dcStrId)
                .dcPdfUrl(PDF_BASE + dcStrId)
                .dcStatus(DcStatus.DRAFT)
                .dcStockApplied(false)
                .build();
        applyRequest(entity, dto);
        if (fileBytes != null && fileBytes.length > 0) setPdf(entity, fileBytes, contentType, fileName);

        DeliveryChallanEntity saved = dcRepository.save(entity);
        logger.info("Delivery challan {} created (dcPrimeId={})", saved.getDcStrId(), saved.getDcPrimeId());
        return toResponseDto(saved);
    }

    // ---------------- UPDATE ----------------
    @Override
    @Transactional
    public DeliveryChallanResponseDto updateDc(Long dcPrimeId, DeliveryChallanRequestDto dto) {
        DeliveryChallanEntity entity = findOrThrow(dcPrimeId);
        requireDraft(entity, "edited");
        validate(dto);
        applyRequest(entity, dto);
        return toResponseDto(dcRepository.save(entity));
    }

    // ---------------- READ ----------------
    @Override
    @Transactional(readOnly = true)
    public DeliveryChallanResponseDto getDcById(Long dcPrimeId) {
        return toResponseDto(findOrThrow(dcPrimeId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<DeliveryChallanResponseDto> getAllDcs(DcStatus status, Long clientPrimeId, String search,
                                                                  int pageNumber, int pageSize) {
        int safePage = Math.max(pageNumber, 1) - 1;
        int safeSize = pageSize <= 0 ? 25 : pageSize;
        Page<DeliveryChallanEntity> page = dcRepository.search(status, clientPrimeId, blankToNull(search),
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "dcCreatedAt")));
        return PagedResponseDto.<DeliveryChallanResponseDto>builder()
                .content(page.getContent().stream().map(this::toResponseDto).collect(Collectors.toList()))
                .pageNumber(pageNumber).pageSize(safeSize)
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .build();
    }

    // ---------------- DELETE ----------------
    @Override
    @Transactional
    public void deleteDc(Long dcPrimeId) {
        DeliveryChallanEntity entity = findOrThrow(dcPrimeId);
        requireDraft(entity, "deleted");
        dcRepository.delete(entity);
        logger.info("Delivery challan {} deleted", entity.getDcStrId());
    }

    // ---------------- STATS ----------------
    @Override
    @Transactional(readOnly = true)
    public DeliveryChallanStatsResponseDto getDcStats() {
        return DeliveryChallanStatsResponseDto.builder()
                .totalCount(dcRepository.count())
                .draftCount(dcRepository.countByDcStatus(DcStatus.DRAFT))
                .dispatchedCount(dcRepository.countByDcStatus(DcStatus.DISPATCHED))
                .deliveredCount(dcRepository.countByDcStatus(DcStatus.DELIVERED))
                .build();
    }

    // ---------------- STATUS (+ STOCK) ----------------
    @Override
    @Transactional
    public DeliveryChallanResponseDto updateDcStatus(Long dcPrimeId, DcStatus status) {
        DeliveryChallanEntity entity = findOrThrow(dcPrimeId);
        if (status == null || NEXT.get(entity.getDcStatus()) != status) {
            throw new InvalidOperationException("Delivery challan " + entity.getDcStrId() + " can't move from "
                    + entity.getDcStatus() + " to " + status + ".");
        }
        if (status == DcStatus.DISPATCHED) {
            applyStock(entity);   // throws on insufficient stock -> whole transaction rolls back
        }
        entity.setDcStatus(status);
        logger.info("Delivery challan {} status -> {}", entity.getDcStrId(), status);
        return toResponseDto(dcRepository.save(entity));
    }

    // ---------------- PDF ----------------
    @Override
    @Transactional
    public DeliveryChallanResponseDto uploadDcPdf(Long dcPrimeId, byte[] fileBytes, String contentType, String fileName) {
        DeliveryChallanEntity entity = findOrThrow(dcPrimeId);
        if (fileBytes == null || fileBytes.length == 0) {
            throw new FileProcessingException("The uploaded PDF appears to be empty. Please choose a valid file.");
        }
        setPdf(entity, fileBytes, contentType, fileName);
        return toResponseDto(dcRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadDto downloadDcPdf(String dcStrId) {
        DeliveryChallanEntity entity = dcRepository.findByDcStrId(dcStrId)
                .orElseThrow(() -> new ResourceNotFoundException("We couldn't find a delivery challan with number " + dcStrId + "."));
        if (entity.getDcPdfBlob() == null) {
            throw new ResourceNotFoundException("No PDF has been generated yet for delivery challan " + dcStrId + ".");
        }
        return FileDownloadDto.builder()
                .data(entity.getDcPdfBlob())
                .contentType(entity.getDcPdfContentType() != null ? entity.getDcPdfContentType() : "application/pdf")
                .fileName(entity.getDcPdfFileName() != null ? entity.getDcPdfFileName() : dcStrId + ".pdf")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<DeliveryChallanSummaryResponseDto> getDcSummaries(int pageNumber, int pageSize) {
        int safePage = Math.max(pageNumber, 1) - 1;
        int safeSize = pageSize <= 0 ? 25 : pageSize;
        Page<DeliveryChallanSummaryResponseDto> page = dcRepository.findDcSummaries(
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "dcPrimeId")));
        return PagedResponseDto.<DeliveryChallanSummaryResponseDto>builder()
                .content(page.getContent()).pageNumber(pageNumber).pageSize(safeSize)
                .totalElements(page.getTotalElements()).totalPages(page.getTotalPages())
                .build();
    }

    // ================= stock =================

    // Reduce from source warehouse; for STOCK_TRANSFER also add to destination. Runs once.
    private void applyStock(DeliveryChallanEntity e) {
        if (Boolean.TRUE.equals(e.getDcStockApplied())) return;
        boolean transfer = e.getDcType() == DcType.STOCK_TRANSFER;
        String remarks = "Delivery Challan " + e.getDcStrId();

        for (DeliveryChallanItemEntity item : e.getDcItems()) {
            InventoryStockUpdateDTO out = stockDto(e.getDcWarehouseStrId(), item.getDcItemQty(), remarks);
            InventoryStockUpdateDTO in = transfer ? stockDto(e.getDcDestWarehouseStrId(), item.getDcItemQty(), remarks) : null;

            if (item.getDcItemVariantId() != null) {
                String variantStrId = variantRepository.findById(item.getDcItemVariantId())
                        .orElseThrow(() -> new ResourceNotFoundException("Variant for item '" + item.getDcItemName() + "' no longer exists."))
                        .getVariantStrId();
                inventoryService.reduceVariantStock(variantStrId, out);
                if (transfer) inventoryService.addVariantStock(variantStrId, in);
            } else {
                String productStrId = productRepository.findById(item.getDcItemProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product for item '" + item.getDcItemName() + "' no longer exists."))
                        .getProductStrId();
                inventoryService.reduceStock(productStrId, out);
                if (transfer) inventoryService.addStock(productStrId, in);
            }
        }
        e.setDcStockApplied(true);
    }

    private InventoryStockUpdateDTO stockDto(String warehouseStrId, BigDecimal qty, String remarks) {
        InventoryStockUpdateDTO d = new InventoryStockUpdateDTO();
        d.setWarehouseStrId(warehouseStrId);
        d.setChangeQty(qty);
        d.setRemarks(remarks);
        return d;
    }

    // ================= helpers =================

    private void requireDraft(DeliveryChallanEntity e, String action) {
        if (e.getDcStatus() != DcStatus.DRAFT) {
            throw new InvalidOperationException("Delivery challan " + e.getDcStrId() + " is " + e.getDcStatus()
                    + " and can't be " + action + ". Only draft challans can be " + action + ".");
        }
    }

    private void setPdf(DeliveryChallanEntity e, byte[] bytes, String contentType, String fileName) {
        e.setDcPdfBlob(bytes);
        e.setDcPdfContentType(contentType != null ? contentType : "application/pdf");
        e.setDcPdfFileName(fileName != null ? fileName : e.getDcStrId() + ".pdf");
        e.setDcPdfUrl(PDF_BASE + e.getDcStrId());
    }

    private DeliveryChallanEntity findOrThrow(Long dcPrimeId) {
        return dcRepository.findById(dcPrimeId)
                .orElseThrow(() -> new ResourceNotFoundException("We couldn't find that delivery challan. It may have been deleted."));
    }

    private void validate(DeliveryChallanRequestDto dto) {
        if (dto.getDcItems() == null || dto.getDcItems().isEmpty()) {
            throw new InvalidOperationException("Please add at least one item to the delivery challan before saving.");
        }
        for (DeliveryChallanItemRequestDto i : dto.getDcItems()) {
            if (i.getDcItemProductId() == null) {
                throw new InvalidOperationException("Every item must be selected from the product list.");
            }
            if (i.getDcItemQty() == null || i.getDcItemQty().compareTo(BigDecimal.ZERO) <= 0
                    || i.getDcItemRate() == null || i.getDcItemRate().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("Every item needs a quantity greater than 0 and a valid rate.");
            }
        }
        if (dto.getDcDate() == null || dto.getDcClientPrimeId() == null || dto.getDcType() == null) {
            throw new InvalidOperationException("Challan date, challan type and client are required.");
        }
        if (dto.getDcType() == DcType.STOCK_TRANSFER && isBlank(dto.getDcDestWarehouseStrId())) {
            throw new InvalidOperationException("Destination warehouse is required for a stock transfer.");
        }
    }

    private void applyRequest(DeliveryChallanEntity e, DeliveryChallanRequestDto d) {
        e.setDcDate(d.getDcDate());
        e.setDcType(d.getDcType());

        e.setDcClientPrimeId(d.getDcClientPrimeId());
        e.setDcClientName(d.getDcClientName());
        e.setDcClientCompanyName(d.getDcClientCompanyName());
        e.setDcClientGstin(d.getDcClientGstin());
        e.setDcClientEmail(d.getDcClientEmail());
        e.setDcClientState(d.getDcClientState());
        e.setDcClientStateCode(d.getDcClientStateCode());
        e.setDcClientCountry(d.getDcClientCountry());
        e.setDcClientBillingAddress(d.getDcClientBillingAddress());
        e.setDcShippingAddress(d.getDcShippingAddress());

        e.setDcCompanyStateCode(d.getDcCompanyStateCode());
        e.setDcPlaceOfSupply(d.getDcPlaceOfSupply());
        e.setDcPlaceOfSupplyStateCode(d.getDcPlaceOfSupplyStateCode());
        e.setDcGstType(resolveGstType(d.getDcCompanyStateCode(), d.getDcPlaceOfSupplyStateCode()));

        e.setDcSoNumber(d.getDcSoNumber());
        e.setDcReferenceNumber(d.getDcReferenceNumber());
        e.setDcProjectCode(d.getDcProjectCode());
        e.setDcNotes(d.getDcNotes());
        e.setDcRoundOff(orDefault(d.getDcRoundOff(), BigDecimal.ZERO));

        WarehouseEntity wh = warehouseService.resolveActiveWarehouse(d.getDcWarehouseStrId());
        e.setDcWarehouseStrId(wh.getWarehouseStrId());
        e.setDcWarehouseName(wh.getWarehouseName());

        if (d.getDcType() == DcType.STOCK_TRANSFER) {
            WarehouseEntity dest = warehouseService.resolveActiveWarehouse(d.getDcDestWarehouseStrId());
            if (dest.getWarehousePrimeId().equals(wh.getWarehousePrimeId())) {
                throw new InvalidOperationException("Source and destination warehouse can't be the same.");
            }
            e.setDcDestWarehouseStrId(dest.getWarehouseStrId());
            e.setDcDestWarehouseName(dest.getWarehouseName());
        } else {
            e.setDcDestWarehouseStrId(null);
            e.setDcDestWarehouseName(null);
        }

        e.getDcItems().clear();
        buildItemsAndTotals(e, d.getDcItems());
    }

    private QuoteGstType resolveGstType(String companyStateCode, String placeOfSupplyStateCode) {
        if (isBlank(companyStateCode) || isBlank(placeOfSupplyStateCode)) {
            throw new InvalidOperationException("Company state code and place of supply state code are required to work out the GST type.");
        }
        return companyStateCode.trim().equals(placeOfSupplyStateCode.trim())
                ? QuoteGstType.CGST_SGST : QuoteGstType.IGST;
    }

    private void buildItemsAndTotals(DeliveryChallanEntity e, List<DeliveryChallanItemRequestDto> dtos) {
        boolean intraState = e.getDcGstType() == QuoteGstType.CGST_SGST;
        BigDecimal subTotal = BigDecimal.ZERO, cgstTotal = BigDecimal.ZERO,
                sgstTotal = BigDecimal.ZERO, igstTotal = BigDecimal.ZERO;

        for (DeliveryChallanItemRequestDto d : dtos) {
            BigDecimal amount = d.getDcItemQty().multiply(d.getDcItemRate()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gstPercent = orDefault(d.getDcItemGstPercent(), BigDecimal.ZERO);
            BigDecimal tax = amount.multiply(gstPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);

            BigDecimal cgst = BigDecimal.ZERO, sgst = BigDecimal.ZERO, igst = BigDecimal.ZERO;
            if (intraState) {
                cgst = tax.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                sgst = tax.subtract(cgst);
            } else {
                igst = tax;
            }

            e.getDcItems().add(DeliveryChallanItemEntity.builder()
                    .dc(e)
                    .dcItemProductId(d.getDcItemProductId())
                    .dcItemVariantId(d.getDcItemVariantId())
                    .dcItemName(d.getDcItemName())
                    .dcItemDescription(d.getDcItemDescription())
                    .dcItemHsnCode(d.getDcItemHsnCode())
                    .dcItemUnit(d.getDcItemUnit())
                    .dcItemQty(d.getDcItemQty())
                    .dcItemRate(d.getDcItemRate())
                    .dcItemAmount(amount)
                    .dcItemGstPercent(gstPercent)
                    .dcItemCgstAmount(cgst)
                    .dcItemSgstAmount(sgst)
                    .dcItemIgstAmount(igst)
                    .dcItemSerial(d.getDcItemSerial())
                    .build());

            subTotal = subTotal.add(amount);
            cgstTotal = cgstTotal.add(cgst);
            sgstTotal = sgstTotal.add(sgst);
            igstTotal = igstTotal.add(igst);
        }

        e.setDcSubTotal(subTotal);
        e.setDcCgstAmount(cgstTotal);
        e.setDcSgstAmount(sgstTotal);
        e.setDcIgstAmount(igstTotal);
        e.setDcTotalAmount(subTotal.add(cgstTotal).add(sgstTotal).add(igstTotal).add(e.getDcRoundOff())
                .setScale(2, RoundingMode.HALF_UP));
    }

    // "DC-26-27-001" - running number resets each financial year (Apr-Mar)
    private String generateNextDcNumber() {
        LocalDate today = LocalDate.now();
        int fyStart = today.getMonthValue() >= 4 ? today.getYear() : today.getYear() - 1;
        String prefix = String.format("DC-%02d-%02d-", fyStart % 100, (fyStart + 1) % 100);
        Integer max = dcRepository.findMaxSequence(prefix, prefix.length() + 1);
        return prefix + String.format("%03d", (max == null ? 0 : max) + 1);
    }

    private DeliveryChallanResponseDto toResponseDto(DeliveryChallanEntity e) {
        List<DeliveryChallanItemResponseDto> items = e.getDcItems().stream()
                .map(i -> DeliveryChallanItemResponseDto.builder()
                        .dcItemPrimeId(i.getDcItemPrimeId())
                        .dcItemProductId(i.getDcItemProductId())
                        .dcItemVariantId(i.getDcItemVariantId())
                        .dcItemName(i.getDcItemName())
                        .dcItemDescription(i.getDcItemDescription())
                        .dcItemHsnCode(i.getDcItemHsnCode())
                        .dcItemUnit(i.getDcItemUnit())
                        .dcItemQty(i.getDcItemQty())
                        .dcItemRate(i.getDcItemRate())
                        .dcItemAmount(i.getDcItemAmount())
                        .dcItemGstPercent(i.getDcItemGstPercent())
                        .dcItemCgstAmount(i.getDcItemCgstAmount())
                        .dcItemSgstAmount(i.getDcItemSgstAmount())
                        .dcItemIgstAmount(i.getDcItemIgstAmount())
                        .dcItemSerial(i.getDcItemSerial())
                        .build())
                .collect(Collectors.toList());

        return DeliveryChallanResponseDto.builder()
                .dcPrimeId(e.getDcPrimeId()).dcStrId(e.getDcStrId()).dcDate(e.getDcDate()).dcType(e.getDcType())
                .dcClientPrimeId(e.getDcClientPrimeId()).dcClientName(e.getDcClientName())
                .dcClientCompanyName(e.getDcClientCompanyName()).dcClientGstin(e.getDcClientGstin())
                .dcClientEmail(e.getDcClientEmail()).dcClientState(e.getDcClientState())
                .dcClientStateCode(e.getDcClientStateCode()).dcClientCountry(e.getDcClientCountry())
                .dcClientBillingAddress(e.getDcClientBillingAddress()).dcShippingAddress(e.getDcShippingAddress())
                .dcCompanyStateCode(e.getDcCompanyStateCode()).dcPlaceOfSupply(e.getDcPlaceOfSupply())
                .dcPlaceOfSupplyStateCode(e.getDcPlaceOfSupplyStateCode()).dcGstType(e.getDcGstType())
                .dcSoNumber(e.getDcSoNumber()).dcReferenceNumber(e.getDcReferenceNumber())
                .dcProjectCode(e.getDcProjectCode())
                .dcWarehouseStrId(e.getDcWarehouseStrId()).dcWarehouseName(e.getDcWarehouseName())
                .dcDestWarehouseStrId(e.getDcDestWarehouseStrId()).dcDestWarehouseName(e.getDcDestWarehouseName())
                .dcSubTotal(e.getDcSubTotal()).dcCgstAmount(e.getDcCgstAmount()).dcSgstAmount(e.getDcSgstAmount())
                .dcIgstAmount(e.getDcIgstAmount()).dcRoundOff(e.getDcRoundOff()).dcTotalAmount(e.getDcTotalAmount())
                .dcStatus(e.getDcStatus()).dcNotes(e.getDcNotes()).dcPdfUrl(e.getDcPdfUrl())
                .dcItems(items).dcCreatedAt(e.getDcCreatedAt()).dcUpdatedAt(e.getDcUpdatedAt())
                .build();
    }

    private BigDecimal orDefault(BigDecimal v, BigDecimal fallback) { return v != null ? v : fallback; }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private String blankToNull(String s) { return isBlank(s) ? null : s.trim(); }
}