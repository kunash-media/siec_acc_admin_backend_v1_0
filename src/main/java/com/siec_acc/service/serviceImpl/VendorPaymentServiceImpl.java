package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.VendorPaymentRequestDto;
import com.siec_acc.dto.response.VendorPaymentResponseDto;
import com.siec_acc.entity.PurchaseBillEntity;
import com.siec_acc.entity.PurchaseBillPaymentEntity;
import com.siec_acc.entity.VendorEntity;
import com.siec_acc.entity.VendorPaymentEntity;
import com.siec_acc.exceptions.InvalidOperationException;
import com.siec_acc.exceptions.ResourceNotFoundException;
import com.siec_acc.repository.PurchaseBillRepository;
import com.siec_acc.repository.VendorPaymentRepository;
import com.siec_acc.repository.VendorRepository;
import com.siec_acc.service.PurchaseOrderClosureService;
import com.siec_acc.service.VendorPaymentService;
import com.siec_acc.utils.StrIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorPaymentServiceImpl implements VendorPaymentService {

    private static final Logger logger = LoggerFactory.getLogger(VendorPaymentServiceImpl.class);

    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Kolkata");
    private static final double EPSILON = 0.005;

    private static final List<String> MODES = List.of("Bank Transfer", "NEFT", "RTGS", "UPI", "Cheque", "Cash");
    private static final Set<String> STATUSES = Set.of("paid", "pending", "bounced", "cleared");
    // Only these statuses are applied to bills (reduce their balance).
    private static final Set<String> APPLIED_STATUSES = Set.of("paid", "cleared");

    private final VendorPaymentRepository vendorPaymentRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseBillRepository purchaseBillRepository;
    private final PurchaseOrderClosureService closureService;

    public VendorPaymentServiceImpl(VendorPaymentRepository vendorPaymentRepository,
                                    VendorRepository vendorRepository,
                                    PurchaseBillRepository purchaseBillRepository,
                                    PurchaseOrderClosureService closureService) {
        this.vendorPaymentRepository = vendorPaymentRepository;
        this.vendorRepository = vendorRepository;
        this.purchaseBillRepository = purchaseBillRepository;
        this.closureService = closureService;
    }

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public VendorPaymentResponseDto createVendorPayment(VendorPaymentRequestDto dto) {
        logger.info("Creating vendor payment | vendorId={} amount={}", dto.getVendorId(), dto.getAmount());
        VendorEntity vendor = findVendorOrThrow(dto.getVendorId());
        String mode = normalizeMode(dto.getMode());
        String status = normalizeStatus(dto.getStatus());
        Map<String, Double> allocs = checkAllocations(dto);

        Map<String, PurchaseBillEntity> bills = lockBills(allocs.keySet(), allocs.keySet());
        assertBillsBelongToVendor(vendor, allocs.keySet(), bills);

        VendorPaymentEntity vp = new VendorPaymentEntity();
        fillHeader(vp, dto, vendor, mode, status);
        vp.replaceAllocations(toAllocationEntities(allocs, bills));

        VendorPaymentEntity saved = vendorPaymentRepository.save(vp);
        saved.setVpStrId(StrIdGenerator.generate("VP", saved.getVpPrimeId()));
        saved.setVpNumber("VP-" + (1000 + saved.getVpPrimeId()));
        saved = vendorPaymentRepository.save(saved);

        if (APPLIED_STATUSES.contains(status)) applyToBills(saved, bills);
        reevaluatePurchaseOrders(bills.values());

        logger.info("Vendor payment created: {}", saved.getVpStrId());
        return toResponse(saved);
    }

    // ------------------------------------------------------------------
    // UPDATE  (undo what the old version applied, then apply the new one)
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public VendorPaymentResponseDto updateVendorPayment(String vpStrId, VendorPaymentRequestDto dto) {
        logger.info("Updating vendor payment: {}", vpStrId);
        VendorPaymentEntity vp = findForUpdateOrThrow(vpStrId);
        VendorEntity vendor = findVendorOrThrow(dto.getVendorId());
        String mode = normalizeMode(dto.getMode());
        String status = normalizeStatus(dto.getStatus());
        Map<String, Double> allocs = checkAllocations(dto);

        Set<String> oldIds = vp.getAllocations().stream()
                .map(VendorPaymentEntity.Allocation::getBillStrId).collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> allIds = new LinkedHashSet<>(oldIds);
        allIds.addAll(allocs.keySet());

        // Old bills may have been deleted meanwhile (pending payments); the new ones must exist.
        Map<String, PurchaseBillEntity> bills = lockBills(allIds, allocs.keySet());
        assertBillsBelongToVendor(vendor, allocs.keySet(), bills);

        revertFromBills(vp, bills.values());

        fillHeader(vp, dto, vendor, mode, status);
        vp.replaceAllocations(toAllocationEntities(allocs, bills));
        VendorPaymentEntity saved = vendorPaymentRepository.save(vp);

        if (APPLIED_STATUSES.contains(status)) applyToBills(saved, bills);
        reevaluatePurchaseOrders(bills.values());

        logger.info("Vendor payment updated: {}", vpStrId);
        return toResponse(saved);
    }

    // ------------------------------------------------------------------
    // DELETE  (bill balances are restored)
    // ------------------------------------------------------------------
    @Override
    @Transactional
    public void deleteVendorPayment(String vpStrId) {
        logger.info("Deleting vendor payment: {}", vpStrId);
        VendorPaymentEntity vp = findForUpdateOrThrow(vpStrId);
        Set<String> ids = vp.getAllocations().stream()
                .map(VendorPaymentEntity.Allocation::getBillStrId).collect(Collectors.toCollection(LinkedHashSet::new));
        Map<String, PurchaseBillEntity> bills = lockBills(ids, Set.of());

        revertFromBills(vp, bills.values());
        vendorPaymentRepository.delete(vp);
        reevaluatePurchaseOrders(bills.values());
        logger.info("Vendor payment deleted: {}", vpStrId);
    }

    // ------------------------------------------------------------------
    // READS
    // ------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public VendorPaymentResponseDto getVendorPayment(String vpStrId) {
        return toResponse(vendorPaymentRepository.findByVpStrId(vpStrId)
                .orElseThrow(() -> new ResourceNotFoundException("No vendor payment found with ID '" + vpStrId + "'.")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorPaymentResponseDto> getAllVendorPayments(String mode, String vendorId, String search) {
        String m = isAll(mode) ? null : mode.trim().toLowerCase(Locale.ROOT);
        String v = isAll(vendorId) ? null : vendorId.trim();
        String q = isBlank(search) ? null : search.trim().toLowerCase(Locale.ROOT);

        return vendorPaymentRepository.findAllByOrderByDateDescVpPrimeIdDesc().stream()
                .filter(p -> m == null || p.getMode().toLowerCase(Locale.ROOT).equals(m))
                .filter(p -> v == null || v.equals(p.getVendorStrId()))
                .filter(p -> q == null
                        || contains(p.getVpNumber(), q) || contains(p.getVpStrId(), q)
                        || contains(p.getVendorName(), q) || contains(p.getReference(), q))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VendorPaymentResponseDto.VendorPaymentStatsResponseDto getStats() {
        YearMonth thisMonth = YearMonth.now(BUSINESS_ZONE);
        List<VendorPaymentEntity> all = vendorPaymentRepository.findAll();
        double paid = 0, month = 0, pending = 0;
        for (VendorPaymentEntity p : all) {
            double amt = p.getAmount() == null ? 0 : p.getAmount();
            if (APPLIED_STATUSES.contains(p.getStatus())) {
                paid += amt;
                if (p.getDate() != null && YearMonth.from(p.getDate()).equals(thisMonth)) month += amt;
            } else if ("pending".equals(p.getStatus())) {
                pending += amt;
            }
        }
        return new VendorPaymentResponseDto.VendorPaymentStatsResponseDto(all.size(), round2(paid), round2(month), round2(pending));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorPaymentResponseDto.VendorOpenBillResponseDto> getOpenBillsOfVendor(String vendorId, String excludeVpStrId) {
        VendorEntity vendor = findVendorOrThrow(vendorId);

        Set<Long> excludeIds = new HashSet<>();
        if (!isBlank(excludeVpStrId)) {
            vendorPaymentRepository.findByVpStrId(excludeVpStrId.trim()).ifPresent(vp -> vp.getAllocations().stream()
                    .map(VendorPaymentEntity.Allocation::getBillPaymentId).filter(Objects::nonNull).forEach(excludeIds::add));
        }

        List<VendorPaymentResponseDto.VendorOpenBillResponseDto> out = new ArrayList<>();
        for (PurchaseBillEntity b : billsOfVendor(vendor)) {
            double balance = balance(b);
            if (!excludeIds.isEmpty()) balance = round2(balance + appliedBy(b, excludeIds));
            if (balance > EPSILON) out.add(new VendorPaymentResponseDto.VendorOpenBillResponseDto(b.getPbStrId(), b.getPbNumber(), balance));
        }
        return out;
    }

    // ==================================================================
    // helpers
    // ==================================================================

    /** Validates the allocation list as such (no DB access) and returns billStrId -> amount in request order. */
    private Map<String, Double> checkAllocations(VendorPaymentRequestDto dto) {
        Map<String, Double> out = new LinkedHashMap<>();
        double total = 0;
        for (VendorPaymentRequestDto.Allocation a : dto.getAllocations()) {
            String id = a.getBillStrId().trim();
            double amt = round2(a.getAmount());
            if (amt <= 0) throw new InvalidOperationException("Every allocated bill needs an amount greater than 0.");
            if (out.put(id, amt) != null) throw new InvalidOperationException("The same bill is allocated more than once.");
            total += amt;
        }
        if (round2(total) > round2(dto.getAmount()) + EPSILON) {
            throw new InvalidOperationException("Total allocated (" + round2(total)
                    + ") exceeds the payment amount (" + round2(dto.getAmount()) + ").");
        }
        return out;
    }

    private void fillHeader(VendorPaymentEntity vp, VendorPaymentRequestDto dto, VendorEntity vendor,
                            String mode, String status) {
        vp.setVendorStrId(vendor.getVendorStrId());
        vp.setVendorName(vendor.getVendorName());
        vp.setDate(dto.getDate());
        vp.setAmount(round2(dto.getAmount()));
        vp.setMode(mode);
        vp.setReference(trimToNull(dto.getReference()));
        vp.setBankAccountName(trimToNull(dto.getBankAccountName()));
        vp.setStatus(status);
        vp.setNotes(trimToNull(dto.getNotes()));
    }

    private List<VendorPaymentEntity.Allocation> toAllocationEntities(Map<String, Double> allocs,
                                                                      Map<String, PurchaseBillEntity> bills) {
        List<VendorPaymentEntity.Allocation> out = new ArrayList<>();
        allocs.forEach((id, amt) -> out.add(new VendorPaymentEntity.Allocation(id, bills.get(id).getPbNumber(), amt)));
        return out;
    }

    /**
     * Locks the bills (sorted, so two requests can never deadlock on each other).
     * Ids in mustExist that are not found raise a 404; other missing ids are skipped.
     */
    private Map<String, PurchaseBillEntity> lockBills(Collection<String> ids, Collection<String> mustExist) {
        Map<String, PurchaseBillEntity> out = new LinkedHashMap<>();
        for (String id : new TreeSet<>(ids)) {
            Optional<PurchaseBillEntity> bill = purchaseBillRepository.findByPbStrIdForUpdate(id);
            if (bill.isPresent()) {
                out.put(id, bill.get());
            } else if (mustExist.contains(id)) {
                throw new ResourceNotFoundException("No purchase bill found with ID '" + id + "'.");
            }
        }
        return out;
    }

    private void assertBillsBelongToVendor(VendorEntity vendor, Collection<String> ids, Map<String, PurchaseBillEntity> bills) {
        for (String id : ids) {
            PurchaseBillEntity b = bills.get(id);
            if (!belongsTo(vendor, b)) {
                throw new InvalidOperationException(b.getPbNumber() + " does not belong to " + vendor.getVendorName() + ".");
            }
        }
    }

    /** Adds one bill payment per allocation, linked back to this vendor payment. */
    private void applyToBills(VendorPaymentEntity vp, Map<String, PurchaseBillEntity> bills) {
        Map<VendorPaymentEntity.Allocation, PurchaseBillPaymentEntity> created = new LinkedHashMap<>();
        for (VendorPaymentEntity.Allocation a : vp.getAllocations()) {
            PurchaseBillEntity bill = bills.get(a.getBillStrId());
            double balance = balance(bill);
            if (balance <= EPSILON) {
                throw new InvalidOperationException(bill.getPbNumber() + " is already fully paid.");
            }
            if (a.getAmount() > balance + EPSILON) {
                throw new InvalidOperationException("Allocation for " + bill.getPbNumber()
                        + " exceeds its balance of " + round2(balance) + ".");
            }
            if (bill.getBillDate() != null && vp.getDate().isBefore(bill.getBillDate())) {
                throw new InvalidOperationException("Payment date cannot be before the bill date of " + bill.getPbNumber() + ".");
            }
            PurchaseBillPaymentEntity p = new PurchaseBillPaymentEntity();
            p.setDate(vp.getDate());
            p.setAmount(round2(a.getAmount()));
            p.setMode(vp.getMode());
            p.setRef(vp.getReference());
            bill.addPayment(p);
            created.put(a, p);
        }
        purchaseBillRepository.flush(); // assigns the ids of the new bill payments
        created.forEach((alloc, pay) -> alloc.setBillPaymentId(pay.getPaymentId()));
    }

    /** Removes every bill payment that this vendor payment created. */
    private void revertFromBills(VendorPaymentEntity vp, Collection<PurchaseBillEntity> bills) {
        Set<Long> ids = vp.getAllocations().stream().map(VendorPaymentEntity.Allocation::getBillPaymentId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        for (PurchaseBillEntity b : bills) {
            b.getPayments().removeIf(p -> p.getPaymentId() != null && ids.contains(p.getPaymentId()));
        }
        vp.getAllocations().forEach(a -> a.setBillPaymentId(null));
        purchaseBillRepository.flush();
    }

    private void reevaluatePurchaseOrders(Collection<PurchaseBillEntity> bills) {
        purchaseBillRepository.flush();
        Set<String> pos = new LinkedHashSet<>();
        for (PurchaseBillEntity b : bills) if (!isBlank(b.getPoNumber())) pos.add(b.getPoNumber());
        for (String po : pos) closureService.reevaluate(po); // a PO closes / re-opens with its bills' payment state
    }

    private List<PurchaseBillEntity> billsOfVendor(VendorEntity vendor) {
        Set<String> names = new LinkedHashSet<>();
        if (!isBlank(vendor.getVendorName())) names.add(vendor.getVendorName().trim());
        if (!isBlank(vendor.getCompanyName())) names.add(vendor.getCompanyName().trim());

        Map<String, PurchaseBillEntity> byId = new LinkedHashMap<>();
        for (String n : names) {
            for (PurchaseBillEntity b : purchaseBillRepository.findByVendorNameIgnoreCaseOrderByCreatedAtDesc(n)) {
                byId.putIfAbsent(b.getPbStrId(), b);
            }
        }
        List<PurchaseBillEntity> list = new ArrayList<>(byId.values());
        list.sort(Comparator.comparing(PurchaseBillEntity::getBillDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(PurchaseBillEntity::getPbPrimeId));
        return list;
    }

    // Bills are tied to vendors by name, so a bill matches when its vendor name equals the vendor's name or company name.
    private boolean belongsTo(VendorEntity vendor, PurchaseBillEntity bill) {
        String n = bill.getVendorName() == null ? "" : bill.getVendorName().trim();
        return n.equalsIgnoreCase(nullToEmpty(vendor.getVendorName()).trim())
                || (!isBlank(vendor.getCompanyName()) && n.equalsIgnoreCase(vendor.getCompanyName().trim()));
    }

    private double appliedBy(PurchaseBillEntity bill, Set<Long> billPaymentIds) {
        return round2(bill.getPayments().stream()
                .filter(p -> p.getPaymentId() != null && billPaymentIds.contains(p.getPaymentId()))
                .mapToDouble(PurchaseBillPaymentEntity::getAmount).sum());
    }

    private double paidAmount(PurchaseBillEntity bill) {
        return round2(bill.getPayments().stream().mapToDouble(PurchaseBillPaymentEntity::getAmount).sum());
    }

    private double balance(PurchaseBillEntity bill) {
        double amt = bill.getAmount() == null ? 0.0 : bill.getAmount();
        return Math.max(0.0, round2(amt - paidAmount(bill)));
    }

    private VendorEntity findVendorOrThrow(String vendorStrId) {
        return vendorRepository.findByVendorStrId(vendorStrId == null ? "" : vendorStrId.trim())
                .orElseThrow(() -> new ResourceNotFoundException("No vendor found with ID '" + vendorStrId + "'."));
    }

    private VendorPaymentEntity findForUpdateOrThrow(String vpStrId) {
        return vendorPaymentRepository.findByVpStrIdForUpdate(vpStrId)
                .orElseThrow(() -> new ResourceNotFoundException("No vendor payment found with ID '" + vpStrId + "'."));
    }

    private String normalizeMode(String raw) {
        String s = raw == null ? "" : raw.trim();
        return MODES.stream().filter(m -> m.equalsIgnoreCase(s)).findFirst()
                .orElseThrow(() -> new InvalidOperationException("Invalid payment mode '" + raw + "'. Allowed: " + String.join(", ", MODES) + "."));
    }

    private String normalizeStatus(String raw) {
        if (isBlank(raw)) return "paid";
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (!STATUSES.contains(s)) {
            throw new InvalidOperationException("Invalid status '" + raw + "'. Allowed: paid, pending, bounced, cleared.");
        }
        return s;
    }

    private VendorPaymentResponseDto toResponse(VendorPaymentEntity p) {
        VendorPaymentResponseDto dto = new VendorPaymentResponseDto();
        dto.setVpStrId(p.getVpStrId());
        dto.setVpNumber(p.getVpNumber());
        dto.setDate(p.getDate());
        dto.setVendorId(p.getVendorStrId());
        dto.setVendorName(p.getVendorName());
        dto.setAmount(p.getAmount());
        dto.setMode(p.getMode());
        dto.setReference(p.getReference());
        dto.setBankAccountName(p.getBankAccountName());
        dto.setStatus(p.getStatus());
        dto.setNotes(p.getNotes());
        dto.setAllocations(p.getAllocations().stream()
                .map(a -> new VendorPaymentResponseDto.Allocation(a.getBillStrId(), a.getBillNumber(), a.getAmount()))
                .collect(Collectors.toList()));
        return dto;
    }

    private boolean isAll(String s) { return isBlank(s) || "all".equalsIgnoreCase(s.trim()); }
    private boolean isBlank(String s) { return s == null || s.isBlank(); }
    private String nullToEmpty(String s) { return s == null ? "" : s; }
    private String trimToNull(String s) { return isBlank(s) ? null : s.trim(); }
    private boolean contains(String hay, String needleLower) { return hay != null && hay.toLowerCase(Locale.ROOT).contains(needleLower); }
    private double round2(double n) { return Math.round(n * 100.0) / 100.0; }
}