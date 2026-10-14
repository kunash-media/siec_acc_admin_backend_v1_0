package com.siec_acc.controller;


import com.siec_acc.dto.request.ExpenseRequestDto;
import com.siec_acc.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // ==================== LIST ====================
    @GetMapping("/get-all-expenses")
    public ResponseEntity<Map<String, Object>> getAllExpenses(
            @RequestParam(value = "status",   required = false, defaultValue = "all") String status,
            @RequestParam(value = "category", required = false, defaultValue = "all") String category,
            @RequestParam(value = "project",  required = false, defaultValue = "all") String project,
            @RequestParam(value = "dateFrom", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo",   required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "search",   required = false) String search,
            @RequestParam(value = "page",     required = false, defaultValue = "1")  int page,
            @RequestParam(value = "size",     required = false, defaultValue = "25") int size,
            @RequestParam(value = "sort",     required = false, defaultValue = "date") String sort,
            @RequestParam(value = "dir",      required = false, defaultValue = "desc") String dir) {

        Map<String, Object> data = expenseService.getAllExpenses(
                status, category, project, dateFrom, dateTo,
                search, page, size, sort, dir);

        return ResponseEntity.ok(ok("Expenses fetched", data));
    }

    // ==================== GET ONE ====================
    @GetMapping("/get-expense/{id}")
    public ResponseEntity<Map<String, Object>> getExpense(@PathVariable String id) {
        return ResponseEntity.ok(ok("Expense fetched", expenseService.getExpenseById(id)));
    }

    // ==================== CREATE ====================
    @PostMapping("/create-expense")
    public ResponseEntity<Map<String, Object>> createExpense(
            @Valid @RequestBody ExpenseRequestDto dto) {
        return ResponseEntity.ok(ok("Expense created", expenseService.createExpense(dto)));
    }

    // ==================== UPLOAD RECEIPT ====================
    @PostMapping(value = "/upload-receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadReceipt(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "expenseId", required = false) String expenseId) {

        String url = expenseService.uploadReceipt(file, expenseId);
        return ResponseEntity.ok(ok("Receipt uploaded", java.util.Map.of("url", url)));
    }

    // ==================== UPDATE ====================
    @PatchMapping("/update-expense/{id}")
    public ResponseEntity<Map<String, Object>> updateExpense(
            @PathVariable String id,
            @Valid @RequestBody ExpenseRequestDto dto) {
        return ResponseEntity.ok(ok("Expense updated", expenseService.updateExpense(id, dto)));
    }

    // ==================== DELETE ONE ====================
    @PatchMapping("/delete-expense/{id}")
    public ResponseEntity<Map<String, Object>> deleteExpense(@PathVariable String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(ok("Expense deleted", null));
    }

    // ==================== BULK DELETE ====================
    @PatchMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDelete(@RequestBody Map<String, List<String>> body) {
        expenseService.bulkDelete(body.get("ids"));
        return ResponseEntity.ok(ok("Expenses deleted", null));
    }

    // ==================== APPROVE / REJECT (single) ====================
    @PatchMapping("/approve-expense/{id}")
    public ResponseEntity<Map<String, Object>> approveExpense(@PathVariable String id) {
        return ResponseEntity.ok(ok("Expense approved",
                expenseService.updateStatus(id, "approved")));
    }

    @PatchMapping("/reject-expense/{id}")
    public ResponseEntity<Map<String, Object>> rejectExpense(@PathVariable String id) {
        return ResponseEntity.ok(ok("Expense rejected",
                expenseService.updateStatus(id, "rejected")));
    }

    // ==================== APPROVE / REJECT (bulk) ====================
    @PatchMapping("/bulk-approve")
    public ResponseEntity<Map<String, Object>> bulkApprove(@RequestBody Map<String, List<String>> body) {
        expenseService.bulkUpdateStatus(body.get("ids"), "approved");
        return ResponseEntity.ok(ok("Expenses approved", null));
    }

    @PatchMapping("/bulk-reject")
    public ResponseEntity<Map<String, Object>> bulkReject(@RequestBody Map<String, List<String>> body) {
        expenseService.bulkUpdateStatus(body.get("ids"), "rejected");
        return ResponseEntity.ok(ok("Expenses rejected", null));
    }

    // ==================== Small response envelope ====================
    private Map<String, Object> ok(String message, Object data) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success", true);
        res.put("message", message);
        res.put("data", data);
        return res;
    }
}