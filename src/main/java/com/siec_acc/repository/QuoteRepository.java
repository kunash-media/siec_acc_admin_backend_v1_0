package com.siec_acc.repository;

import com.siec_acc.dto.response.QuoteSummaryResponseDto;
import com.siec_acc.entity.QuoteEntity;
import com.siec_acc.enum_status.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface QuoteRepository extends JpaRepository<QuoteEntity, Long> {

    Optional<QuoteEntity> findByQuoteStrId(String quoteStrId);

    long countByQuoteStatus(QuoteStatus status);

    @Query("SELECT q FROM QuoteEntity q WHERE " +
            "(:status IS NULL OR q.quoteStatus = :status) " +
            "AND (:clientPrimeId IS NULL OR q.quoteClientPrimeId = :clientPrimeId) " +
            "AND (:search IS NULL OR LOWER(q.quoteStrId) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(q.quoteClientName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(q.quoteClientCompanyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(q.quoteReferenceNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<QuoteEntity> search(@Param("status") QuoteStatus status,
                             @Param("clientPrimeId") Long clientPrimeId,
                             @Param("search") String search,
                             Pageable pageable);

    @Query("SELECT COALESCE(SUM(q.quoteTotalAmountInr), 0) FROM QuoteEntity q")
    BigDecimal sumTotalValueInr();

    // Highest running number for a financial-year prefix, e.g. "QUO-25-26-"
    @Query("SELECT MAX(CAST(SUBSTRING(q.quoteStrId, :startPos) AS integer)) FROM QuoteEntity q " +
            "WHERE q.quoteStrId LIKE CONCAT(:prefix, '%')")
    Integer findMaxSequence(@Param("prefix") String prefix, @Param("startPos") int startPos);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE QuoteEntity q SET q.quoteStatus = :expired, q.quoteUpdatedAt = :now " +
            "WHERE q.quoteStatus = :sent AND q.quoteValidTill < :today")
    int expireOverdue(@Param("sent") QuoteStatus sent,
                      @Param("expired") QuoteStatus expired,
                      @Param("today") LocalDate today,
                      @Param("now") LocalDateTime now);

    // Constructor projection: skips items and the PDF blob entirely
    @Query("SELECT new com.siec_acc.dto.response.QuoteSummaryResponseDto(" +
            "q.quotePrimeId, q.quoteStrId, q.quoteValidTill, q.quoteClientPrimeId, q.quoteStatus) " +
            "FROM QuoteEntity q")
    Page<QuoteSummaryResponseDto> findQuoteSummaries(Pageable pageable);
}