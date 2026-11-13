package com.siec_acc.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Payload shapes for the Dashboard page. All amounts are INR; *Display / compact / full are ready-to-print rupee strings. */
public final class DashboardDto {

    private DashboardDto() {}

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Summary {
        private LocalDate asOf;
        private String periodLabel;       // e.g. "Nov 2026 (month to date)"
        private String comparisonLabel;   // e.g. "vs 1-13 Oct"
        private Metric sales;
        private Metric purchases;
        private Aging receivables;
        private Aging payables;
        private Cash bank;
        private Gst gst;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Metric {
        private BigDecimal value;
        private String compact;           // ₹42.5L
        private String full;              // ₹42,50,000
        private BigDecimal previousValue;
        private BigDecimal trendPct;      // null when there is no previous-period baseline
        private String trendDirection;    // UP | DOWN | FLAT | NEW
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Aging {
        private BigDecimal totalUnpaid;
        private String totalUnpaidCompact;
        private String totalUnpaidFull;
        private BigDecimal current;
        private String currentFull;
        private BigDecimal overdue;
        private String overdueFull;
        private int overdueCount;
        private BigDecimal overduePercent; // overdue / totalUnpaid * 100, drives the progress bar
        private LocalDate nextDueDate;     // earliest not-yet-overdue due date (null if none)
        private Integer nextDueInDays;     // 0 = due today
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Cash {
        private BigDecimal value;
        private String compact;
        private String full;
        private String basis;              // always DERIVED_CASH_FLOW until a bank ledger exists
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Gst {
        private BigDecimal outputGst;
        private BigDecimal inputGst;
        private BigDecimal payable;        // output - input (negative = credit)
        private String compact;            // absolute value
        private String full;
        private String direction;          // PAYABLE | CREDIT
        private LocalDate dueDate;
        private int dueInDays;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class IncomeExpense {
        private String period;
        private String periodLabel;
        private String rangeLabel;         // "Apr 2026 - Nov 2026"
        private List<String> months;       // parallel arrays, one entry per month
        private List<BigDecimal> income;
        private List<BigDecimal> expense;
        private List<BigDecimal> net;
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal totalNet;
        private String totalIncomeDisplay;
        private String totalExpenseDisplay;
        private String totalNetDisplay;    // signed, e.g. +₹2,67,000
        private boolean netPositive;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TopExpenses {
        private String period;
        private String periodLabel;
        private String rangeLabel;
        private BigDecimal total;
        private String totalDisplay;
        private List<ExpenseSlice> items;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExpenseSlice {
        private String label;
        private BigDecimal amount;
        private String amountDisplay;
        private BigDecimal percent;        // one decimal; slices always add up to exactly 100.0
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ActiveProject {
        private String id;                 // projectStrId, e.g. PRJ-001
        private String code;
        private String name;
        private String customer;
        private String status;
        private String projectManager;
        private LocalDate startDate;
        private LocalDate endDate;
        private int progress;              // 0-100
        private BigDecimal budget;
        private BigDecimal revenue;
        private BigDecimal actualCost;
        private BigDecimal profit;
        private BigDecimal budgetUsagePct;
        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Alert {
        private String type;              // LOW_STOCK | INVOICE_OVERDUE | PO_APPROVAL | PAYMENT_RECEIVED | PROJECT_BUDGET
        private String severity;          // red | amber | blue | green | purple (matches the existing .al-* CSS classes)
        private String title;
        private String description;
        private LocalDateTime occurredAt; // may be null
        private String link;              // may be null
    }
}