package com.siec_acc.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class CrmDtos {
    private CrmDtos() {}

    // ---------- requests ----------
    public record ContactedRequest(@NotBlank String note, LocalDate nextFollowup) {}
    public record ConvertDealRequest(@NotBlank String title, BigDecimal amount, LocalDate expectedClose, String stage) {}
    public record DealUpdateRequest(@NotBlank String title, BigDecimal amount, LocalDate expectedClose, String notes) {}
    public record StageRequest(@NotBlank String stage) {}
    public record WonRequest(@NotNull @Positive BigDecimal amount) {}
    public record LostRequest(@NotBlank String reason, String remarks) {}

    // ---------- responses ----------
    public record DealResponse(Long dealPrimeId, String dealNumber, Long leadPrimeId, String leadNo,
                               String title, String company, String contact, BigDecimal amount,
                               String stage, int probability, LocalDate expectedClose, LocalDate closedAt,
                               String notes, String lostReason, LocalDateTime createdAt) {}

    public record DealStats(long open, BigDecimal pipelineValue, long won, BigDecimal wonValue,
                            BigDecimal avgWon, long lost) {}

    public record ActivityResponse(Long id, LocalDateTime at, String leadNo, String company,
                                   String type, String ref, String action, String details) {}

    public record ActivityStats(long total, long today, long leadsConverted, long dealsWon) {}

    public record ServiceCount(String name, long count) {}

    public record DashboardSummary(long totalLeads, long todayFollowups, long totalFollowups,
                                   long won, long lost, List<ServiceCount> byService) {}

    public record PageResponse<T>(List<T> items, long totalItems, int totalPages, int page, int size) {
        public static <T> PageResponse<T> of(Page<T> p) {
            return new PageResponse<>(p.getContent(), p.getTotalElements(),
                    Math.max(1, p.getTotalPages()), p.getNumber(), p.getSize());
        }
    }
}