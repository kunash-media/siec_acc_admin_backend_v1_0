package com.siec_acc.service;



import com.siec_acc.dto.request.ExpenseRequestDto;
import com.siec_acc.dto.response.ExpenseResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpenseService {

    Map<String, Object> getAllExpenses(
            String status, String category, String projectId,
            LocalDate dateFrom, LocalDate dateTo,
            String search, int page, int size, String sortField, String sortDir);

    ExpenseResponseDto getExpenseById(String id);

    ExpenseResponseDto createExpense(ExpenseRequestDto dto);

    ExpenseResponseDto updateExpense(String id, ExpenseRequestDto dto);

    void deleteExpense(String id);

    void bulkDelete(List<String> ids);

    ExpenseResponseDto updateStatus(String id, String status);

    String uploadReceipt(MultipartFile file, String expenseId);

    void bulkUpdateStatus(List<String> ids, String status);
}