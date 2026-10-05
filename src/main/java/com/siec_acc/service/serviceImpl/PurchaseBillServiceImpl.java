package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.PurchaseBillPatchDto;
import com.siec_acc.dto.request.PurchaseBillPaymentRequestDto;
import com.siec_acc.dto.request.PurchaseBillRequestDto;
import com.siec_acc.dto.response.PurchaseBillPaymentResponseDto;
import com.siec_acc.dto.response.PurchaseBillResponseDto;
import com.siec_acc.entity.PurchaseBillEntity;
import com.siec_acc.entity.PurchaseBillPaymentEntity;
import com.siec_acc.exceptions.InvalidOperationException;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.GoodsReceiptRepository;
import com.siec_acc.repository.PurchaseBillRepository;
import com.siec_acc.repository.PurchaseOrderRepository;
import com.siec_acc.service.PurchaseBillService;
import com.siec_acc.utils.StrIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PurchaseBillServiceImpl implements PurchaseBillService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseBillServiceImpl.class);

    // Server may run in UTC; "today" for overdue calculation is India time (same convention as PurchaseServiceImpl).
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");

    private static final Set<String> ALLOWED_STATUSES = Set.of("received", "partial", "paid", "overdue");
    private static final double EPSILON = 0.005; // guards against floating-point rounding at the paid/balance boundary

    private final PurchaseBillRepository purchaseBillRepository;
    private final PurchaseOrderRepository purchaseOrderRepository; // to cross-check vendor when a PO is linked
    private final GoodsReceiptRepository goodsReceiptRepository;   // a bill against a PO needs goods received first

    public PurchaseBillServiceImpl(PurchaseBillRepository purchaseBillRepository,
                                   PurchaseOrderRepository purchaseOrderRepository,
                                   GoodsReceiptRepository goodsReceiptRepository) {
        this.purchaseBillRepository = purchaseBillRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public PurchaseBillResponseDto createPurchaseBill(PurchaseBillRequestDto dto) {
        logger.info("Creating new purchase bill for vendor: {}", dto.getVendorName());

        validateDates(dto.getBillDate(), dto.getDueDate());
        validatePoVendorMatch(dto.getPoNumber(), dto.getVendorName());

        PurchaseBillEntity bill = new PurchaseBillEntity();
        bill.setVendorName(dto.getVendorName().trim());
        bill.setPoNumber(trimToNull(dto.getPoNumber()));
        bill.setBillDate(dto.getBillDate());
        bill.setDueDate(dto.getDueDate());
        bill.setAmount(dto.getAmount());

        PurchaseBillEntity saved = purchaseBillRepository.save(bill);
        saved.setPbStrId(StrIdGenerator.generate("PB", saved.getPbPrimeId()));
        saved.setPbNumber("PB-" + (4000 + saved.getPbPrimeId()));
        saved = purchaseBillRepository.save(saved);

        logger.info("PurchaseBillEntity created successfully: {}", saved.getPbStrId());
        return mapToResponse(saved);
    }

    // ------------------------------------------------------------------
    // UPDATE (PUT) — pbNumber is never changed here; amount cannot drop below what's already paid.
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public PurchaseBillResponseDto updatePurchaseBill(String pbStrId, PurchaseBillRequestDto dto) {
        logger.info("Updating purchase bill: {}", pbStrId);
        PurchaseBillEntity bill = getEntityOrThrow(pbStrId);

        validateDates(dto.getBillDate(), dto.getDueDate());
        validatePoVendorMatch(dto.getPoNumber(), dto.getVendorName());

        double paidSoFar = paidAmount(bill);
        if (dto.getAmount() < paidSoFar) {
            throw new InvalidOperationException(
                    "Bill amount cannot be less than the " + round2(paidSoFar) + " already paid.");
        }

        bill.setVendorName(dto.getVendorName().trim());
        bill.setPoNumber(trimToNull(dto.getPoNumber()));
        bill.setBillDate(dto.getBillDate());
        bill.setDueDate(dto.getDueDate());
        bill.setAmount(dto.getAmount());

        PurchaseBillEntity updated = purchaseBillRepository.save(bill);
        logger.info("PurchaseBillEntity updated successfully: {}", pbStrId);
        return mapToResponse(updated);
    }

    // ------------------------------------------------------------------
    // PATCH — only non-null fields are applied
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public PurchaseBillResponseDto patchPurchaseBill(String pbStrId, PurchaseBillPatchDto dto) {
        logger.info("Patching purchase bill: {}", pbStrId);
        PurchaseBillEntity bill = getEntityOrThrow(pbStrId);

        LocalDate newBillDate = dto.getBillDate() != null ? dto.getBillDate() : bill.getBillDate();
        LocalDate newDueDate = dto.getDueDate() != null ? dto.getDueDate() : bill.getDueDate();
        validateDates(newBillDate, newDueDate);

        String newVendor = dto.getVendorName() != null ? dto.getVendorName().trim() : bill.getVendorName();
        String newPoNumber = dto.getPoNumber() != null ? trimToNull(dto.getPoNumber()) : bill.getPoNumber();
        if (dto.getPoNumber() != null) {
            validatePoVendorMatch(newPoNumber, newVendor);
        }

        if (dto.getAmount() != null) {
            double paidSoFar = paidAmount(bill);
            if (dto.getAmount() < paidSoFar) {
                throw new InvalidOperationException(
                        "Bill amount cannot be less than the " + round2(paidSoFar) + " already paid.");
            }
            bill.setAmount(dto.getAmount());
        }

        bill.setVendorName(newVendor);
        bill.setPoNumber(newPoNumber);
        bill.setBillDate(newBillDate);
        bill.setDueDate(newDueDate);

        PurchaseBillEntity patched = purchaseBillRepository.save(bill);
        logger.info("PurchaseBillEntity patched successfully: {}", pbStrId);
        return mapToResponse(patched);
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public void deletePurchaseBill(String pbStrId) {
        logger.info("Deleting purchase bill: {}", pbStrId);
        PurchaseBillEntity bill = getEntityOrThrow(pbStrId);
        purchaseBillRepository.delete(bill);
        logger.info("PurchaseBillEntity deleted successfully: {}", pbStrId);
    }

    // ------------------------------------------------------------------
    // READS
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PurchaseBillResponseDto getPurchaseBillByStrId(String pbStrId) {
        logger.info("Fetching purchase bill: {}", pbStrId);
        return mapToResponse(getEntityOrThrow(pbStrId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseBillResponseDto> getAllPurchaseBills() {
        logger.info("Fetching all purchase bills");
        return purchaseBillRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Status is derived, not a DB column, so filtering happens in memory after loading (fine at this data scale).
    @Override
    @Transactional(readOnly = true)
    public List<PurchaseBillResponseDto> getPurchaseBillsByStatus(String status) {
        String normalized = normalizeStatus(status);
        logger.info("Fetching purchase bills with status: {}", normalized);
        return purchaseBillRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .filter(b -> normalized.equalsIgnoreCase(b.getStatus()))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------
    // PAYMENTS
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public PurchaseBillResponseDto recordPayment(String pbStrId, PurchaseBillPaymentRequestDto dto) {
        logger.info("Recording payment for purchase bill: {}", pbStrId);
        PurchaseBillEntity bill = getEntityOrThrow(pbStrId);

        double balance = balance(bill);
        if (balance <= EPSILON) {
            throw new InvalidOperationException("This bill is already fully paid.");
        }
        if (dto.getAmount() > balance + EPSILON) {
            throw new InvalidOperationException("Payment exceeds the balance of " + round2(balance) + ".");
        }

        PurchaseBillPaymentEntity payment = new PurchaseBillPaymentEntity();
        payment.setDate(dto.getDate());
        payment.setAmount(dto.getAmount());
        payment.setMode(trimToNull(dto.getMode()));
        payment.setRef(trimToNull(dto.getRef()));
        bill.addPayment(payment);

        PurchaseBillEntity saved = purchaseBillRepository.save(bill);
        closePoIfSettled(saved);
        logger.info("Payment of {} recorded against {}", round2(dto.getAmount()), bill.getPbNumber());
        return mapToResponse(saved);
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------
    /** Last step of the flow: PO is fully received and every bill raised against it is fully paid -> closed. */
    private void closePoIfSettled(PurchaseBillEntity bill) {
        if (isBlank(bill.getPoNumber())) return;
        purchaseOrderRepository.findByPoNumber(bill.getPoNumber().trim()).ifPresent(po -> {
            if (!"fully_received".equalsIgnoreCase(po.getStatus())) return;
            boolean allPaid = purchaseBillRepository.findByPoNumberIgnoreCaseOrderByCreatedAtDesc(po.getPoNumber())
                    .stream().allMatch(b -> balance(b) <= EPSILON);
            if (allPaid) {
                po.setStatus("closed");
                purchaseOrderRepository.save(po);
                logger.info("{} closed: fully received and fully paid", po.getPoNumber());
            }
        });
    }

    private PurchaseBillEntity getEntityOrThrow(String pbStrId) {
        return purchaseBillRepository.findByPbStrId(pbStrId)
                .orElseThrow(() -> {
                    logger.warn("PurchaseBillEntity not found: {}", pbStrId);
                    return new ResourceNotFoundException("No purchase bill found with ID '" + pbStrId + "'.");
                });
    }

    private void validateDates(LocalDate billDate, LocalDate dueDate) {
        if (billDate != null && dueDate != null && dueDate.isBefore(billDate)) {
            throw new InvalidOperationException("Due date cannot be before the bill date.");
        }
    }

    /** If a PO number is given, it must exist and its vendor must match the bill's vendor. */
    private void validatePoVendorMatch(String poNumber, String vendorName) {
        if (isBlank(poNumber)) return;
        purchaseOrderRepository.findByPoNumber(poNumber.trim()).ifPresent(po -> {
            if (!po.getVendorName().equalsIgnoreCase(vendorName)) {
                throw new InvalidOperationException(
                        "Vendor does not match " + po.getPoNumber() + " (" + po.getVendorName() + ").");
            }
            if (!goodsReceiptRepository.existsByPoStrId(po.getPoStrId())) {
                throw new InvalidOperationException(
                        "No goods have been received against " + po.getPoNumber() + " yet. Record a goods receipt before billing it.");
            }
        });
    }

    private double paidAmount(PurchaseBillEntity bill) {
        return round2(bill.getPayments().stream().mapToDouble(PurchaseBillPaymentEntity::getAmount).sum());
    }

    private double balance(PurchaseBillEntity bill) {
        double amt = bill.getAmount() == null ? 0.0 : bill.getAmount();
        return Math.max(0.0, round2(amt - paidAmount(bill)));
    }

    /** Mirrors the frontend's pbStatus(): paid > overdue > partial > received (unpaid). */
    private String deriveStatus(PurchaseBillEntity bill) {
        double amt = bill.getAmount() == null ? 0.0 : bill.getAmount();
        double bal = balance(bill);
        if (amt > 0 && bal <= EPSILON) return "paid";
        if (bill.getDueDate() != null && bill.getDueDate().isBefore(LocalDate.now(BUSINESS_ZONE))) return "overdue";
        if (paidAmount(bill) > 0) return "partial";
        return "received";
    }

    private String normalizeStatus(String raw) {
        String s = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(s)) {
            throw new InvalidOperationException(
                    "Invalid status '" + raw + "'. Allowed: received, partial, paid, overdue.");
        }
        return s;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private double round2(double n) {
        return Math.round(n * 100.0) / 100.0;
    }

    private PurchaseBillResponseDto mapToResponse(PurchaseBillEntity bill) {
        List<PurchaseBillPaymentResponseDto> paymentDtos = bill.getPayments().stream()
                .map(p -> new PurchaseBillPaymentResponseDto(p.getPaymentId(), p.getDate(), p.getAmount(), p.getMode(), p.getRef()))
                .collect(Collectors.toList());

        double paid = paidAmount(bill);
        double bal = balance(bill);
        String status = deriveStatus(bill);

        return new PurchaseBillResponseDto(
                bill.getPbPrimeId(),
                bill.getPbStrId(),
                bill.getPbNumber(),
                bill.getVendorName(),
                bill.getPoNumber(),
                bill.getBillDate(),
                bill.getDueDate(),
                bill.getAmount(),
                paid,
                bal,
                status,
                paymentDtos,
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }
}


