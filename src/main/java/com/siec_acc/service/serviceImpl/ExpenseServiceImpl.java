package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.ExpenseRequestDto;
import com.siec_acc.dto.response.ExpenseResponseDto;
import com.siec_acc.entity.ExpenseEntity;
import com.siec_acc.repository.ExpenseRepository;
import com.siec_acc.service.ExpenseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:9093}")
    private String uploadBaseUrl;

    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    // ============================================================
    // LIST
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAllExpenses(
            String status, String category, String projectId,
            LocalDate dateFrom, LocalDate dateTo,
            String search, int page, int size, String sortField, String sortDir) {

        String statusFilter   = normalizeFilter(status);
        String categoryFilter = normalizeFilter(category);
        String projectFilter  = normalizeFilter(projectId);
        String searchFilter   = (search == null || search.trim().isEmpty()) ? null : search.trim();

        int safePage = Math.max(1, page);
        int safeSize = (size <= 0) ? 25 : Math.min(size, 200);
        Sort sort = buildSort(sortField, sortDir);
        Pageable pageable = PageRequest.of(safePage - 1, safeSize, sort);

        Page<ExpenseEntity> pageResult = expenseRepository.search(
                statusFilter, categoryFilter, projectFilter,
                dateFrom, dateTo, searchFilter, pageable);

        List<ExpenseResponseDto> rows = new ArrayList<>();
        for (ExpenseEntity e : pageResult.getContent()) rows.add(toResponseDto(e));

        Map<String, Object> pagination = new LinkedHashMap<>();
        pagination.put("page",        safePage);
        pagination.put("size",        safeSize);
        pagination.put("totalItems",  pageResult.getTotalElements());
        pagination.put("totalPages",  pageResult.getTotalPages());
        pagination.put("hasNext",     pageResult.hasNext());
        pagination.put("hasPrevious", pageResult.hasPrevious());

        Object[] row = expenseRepository.aggregate(
                statusFilter, categoryFilter, projectFilter,
                dateFrom, dateTo, searchFilter);

        Map<String, Object> stats = new LinkedHashMap<>();
        if (row != null && row.length > 0 && row[0] != null) {
            Object[] cols = (Object[]) row[0];
            stats.put("totalExpenses", ((Number) cols[0]).longValue());
            stats.put("totalAmount",   toBigDecimal(cols[1]));
            stats.put("pendingCount",  cols[2] == null ? 0L : ((Number) cols[2]).longValue());
            stats.put("approvedCount", cols[3] == null ? 0L : ((Number) cols[3]).longValue());
            stats.put("rejectedCount", cols[4] == null ? 0L : ((Number) cols[4]).longValue());
        } else {
            stats.put("totalExpenses", 0L);
            stats.put("totalAmount",   BigDecimal.ZERO);
            stats.put("pendingCount",  0L);
            stats.put("approvedCount", 0L);
            stats.put("rejectedCount", 0L);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("expenses",   rows);
        out.put("pagination", pagination);
        out.put("stats",      stats);
        return out;
    }

    // ============================================================
    // GET ONE
    // ============================================================
    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDto getExpenseById(String id) {
        return toResponseDto(findByIdOrThrow(id));
    }

    // ============================================================
    // CREATE
    // ============================================================
    @Override
    public ExpenseResponseDto createExpense(ExpenseRequestDto dto) {
        validateAmount(dto.getAmount());
        validateStatus(dto.getStatus());

        ExpenseEntity e = new ExpenseEntity();
        e.setExpenseStrId(generateExpenseId());
        applyDto(e, dto);
        return toResponseDto(expenseRepository.save(e));
    }

    // ============================================================
    // UPDATE
    // ============================================================
    @Override
    public ExpenseResponseDto updateExpense(String id, ExpenseRequestDto dto) {
        validateAmount(dto.getAmount());
        validateStatus(dto.getStatus());

        ExpenseEntity e = findByIdOrThrow(id);
        applyDto(e, dto);
        return toResponseDto(expenseRepository.save(e));
    }

    // ============================================================
    // DELETE
    // ============================================================
    @Override
    public void deleteExpense(String id) {
        ExpenseEntity e = findByIdOrThrow(id);
        deleteLocalReceiptFile(e.getExpenseReceipt());
        expenseRepository.delete(e);
    }

    @Override
    public void bulkDelete(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<ExpenseEntity> toDelete = new ArrayList<>();
        for (String id : ids) {
            expenseRepository.findByExpenseStrId(id).ifPresent(toDelete::add);
        }
        for (ExpenseEntity e : toDelete) deleteLocalReceiptFile(e.getExpenseReceipt());
        if (!toDelete.isEmpty()) expenseRepository.deleteAll(toDelete);
    }

    // ============================================================
    // STATUS
    // ============================================================
    @Override
    public ExpenseResponseDto updateStatus(String id, String status) {
        validateStatus(status);
        ExpenseEntity e = findByIdOrThrow(id);
        e.setExpenseStatus(status.toLowerCase(Locale.ROOT));
        return toResponseDto(expenseRepository.save(e));
    }

    @Override
    public void bulkUpdateStatus(List<String> ids, String status) {
        if (ids == null || ids.isEmpty()) return;
        validateStatus(status);
        String normalized = status.toLowerCase(Locale.ROOT);

        List<ExpenseEntity> updated = new ArrayList<>();
        for (String id : ids) {
            expenseRepository.findByExpenseStrId(id).ifPresent(e -> {
                if ("pending".equals(e.getExpenseStatus())) {
                    e.setExpenseStatus(normalized);
                    updated.add(e);
                }
            });
        }
        if (!updated.isEmpty()) expenseRepository.saveAll(updated);
    }

    // ============================================================
    // RECEIPT UPLOAD — inlined file I/O, no separate service
    // ============================================================
    @Override
    public String uploadReceipt(MultipartFile file, String expenseId) {
        if (file == null || file.isEmpty()) return null;
        if (file.getSize() > 5L * 1024 * 1024) {
            throw new IllegalArgumentException("Receipt file must be under 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Please upload an image file");
        }

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase(Locale.ROOT);

        String safeId = (expenseId == null || expenseId.isBlank())
                ? "expense"
                : expenseId.replaceAll("[^a-zA-Z0-9_-]", "");
        String fileName = safeId + "_" + Instant.now().toEpochMilli() + ext;

        try {
            Path dir = Paths.get(uploadDir, "receipts").toAbsolutePath().normalize();
            Files.createDirectories(dir);
            Path target = dir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store receipt: " + ex.getMessage(), ex);
        }

        // strip trailing slash from base url if present
        String base = uploadBaseUrl.endsWith("/")
                ? uploadBaseUrl.substring(0, uploadBaseUrl.length() - 1)
                : uploadBaseUrl;
        return base + "/uploads/receipts/" + fileName;
    }

    /** Deletes the physical file for a stored URL — only if we produced it. */
    private void deleteLocalReceiptFile(String url) {
        if (url == null || url.isBlank()) return;
        String marker = "/uploads/receipts/";
        int idx = url.indexOf(marker);
        if (idx < 0) return;
        String fileName = url.substring(idx + marker.length());
        try {
            Path target = Paths.get(uploadDir, "receipts").toAbsolutePath().normalize().resolve(fileName);
            Files.deleteIfExists(target);
        } catch (IOException ignored) { }
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private ExpenseEntity findByIdOrThrow(String strId) {
        if (strId == null || strId.isBlank()) {
            throw new IllegalArgumentException("Expense id is required");
        }
        return expenseRepository.findByExpenseStrId(strId)
                .orElseThrow(() -> new RuntimeException("Expense not found: " + strId));
    }

    private void applyDto(ExpenseEntity e, ExpenseRequestDto dto) {
        e.setExpenseDate(dto.getDate());
        e.setExpenseCategory(dto.getCategory());
        e.setExpenseDescription(dto.getDescription());
        e.setExpenseAmount(dto.getAmount());
        e.setExpenseTax(dto.getTax() == null ? BigDecimal.ZERO : dto.getTax());
        e.setExpenseVendor(dto.getVendor());
        e.setExpensePaymentAccount(
                dto.getPaymentAccount() == null ? "Bank" : dto.getPaymentAccount());
        e.setExpenseStatus(
                dto.getStatus() == null ? "pending" : dto.getStatus().toLowerCase(Locale.ROOT));
        e.setExpenseProjectId(dto.getProjectId());
        e.setExpenseNotes(dto.getNotes());
        if (dto.getReceipt() != null) e.setExpenseReceipt(dto.getReceipt());
    }

    private ExpenseResponseDto toResponseDto(ExpenseEntity e) {
        ExpenseResponseDto dto = new ExpenseResponseDto();
        dto.setId(e.getExpenseStrId());
        dto.setPrimeId(e.getExpensePrimeId());
        dto.setDate(e.getExpenseDate());
        dto.setCategory(e.getExpenseCategory());
        dto.setDescription(e.getExpenseDescription());
        dto.setAmount(e.getExpenseAmount());
        dto.setTax(e.getExpenseTax() == null ? BigDecimal.ZERO : e.getExpenseTax());

        BigDecimal taxAmt = computeTaxAmount(e.getExpenseAmount(), e.getExpenseTax());
        dto.setTaxAmount(taxAmt);
        dto.setTotal(e.getExpenseAmount().add(taxAmt));

        dto.setVendor(e.getExpenseVendor());
        dto.setPaymentAccount(e.getExpensePaymentAccount());
        dto.setStatus(e.getExpenseStatus());
        dto.setProjectId(e.getExpenseProjectId());
        dto.setProjectName(null);
        dto.setNotes(e.getExpenseNotes());
        dto.setReceipt(e.getExpenseReceipt());
        dto.setCreatedAt(e.getExpenseCreatedAt());
        dto.setUpdatedAt(e.getExpenseUpdatedAt());
        return dto;
    }

    private BigDecimal computeTaxAmount(BigDecimal amount, BigDecimal taxPct) {
        if (amount == null) return BigDecimal.ZERO;
        if (taxPct == null || taxPct.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return amount.multiply(taxPct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private String generateExpenseId() {
        Long max = expenseRepository.findMaxPrimeId();
        long next = (max == null ? 0 : max) + 1;
        return String.format("EXP-%03d", next);
    }

    private String normalizeFilter(String s) {
        if (s == null || s.isBlank() || "all".equalsIgnoreCase(s)) return null;
        return s.trim();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }

    private void validateStatus(String status) {
        if (status == null) return;
        String s = status.toLowerCase(Locale.ROOT);
        if (!s.equals("pending") && !s.equals("approved") && !s.equals("rejected")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    private Sort buildSort(String field, String dir) {
        String f = (field == null || field.isBlank()) ? "expenseDate" : field;
        switch (f) {
            case "id":       f = "expenseStrId";    break;
            case "date":     f = "expenseDate";     break;
            case "category": f = "expenseCategory"; break;
            case "amount":   f = "expenseAmount";   break;
            case "status":   f = "expenseStatus";   break;
            default:         f = "expenseDate";
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(dir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, f);
    }
}