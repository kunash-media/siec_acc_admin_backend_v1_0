package com.siec_acc.dto.response;

import com.siec_acc.enum_status.QuoteStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
public class QuoteSummaryResponseDto {
    private Long quotePrimeId;
    private String quoteStrId;
    private LocalDate quoteValidTill;
    private Long quoteClientPrimeId;
    private QuoteStatus quoteStatus;

    public QuoteSummaryResponseDto(Long quotePrimeId, String quoteStrId, LocalDate quoteValidTill, Long quoteClientPrimeId, QuoteStatus quoteStatus) {
        this.quotePrimeId = quotePrimeId;
        this.quoteStrId = quoteStrId;
        this.quoteValidTill = quoteValidTill;
        this.quoteClientPrimeId = quoteClientPrimeId;
        this.quoteStatus = quoteStatus;
    }

    public QuoteSummaryResponseDto(){}

    public Long getQuotePrimeId() {
        return quotePrimeId;
    }

    public void setQuotePrimeId(Long quotePrimeId) {
        this.quotePrimeId = quotePrimeId;
    }

    public String getQuoteStrId() {
        return quoteStrId;
    }

    public void setQuoteStrId(String quoteStrId) {
        this.quoteStrId = quoteStrId;
    }

    public LocalDate getQuoteValidTill() {
        return quoteValidTill;
    }

    public void setQuoteValidTill(LocalDate quoteValidTill) {
        this.quoteValidTill = quoteValidTill;
    }

    public Long getQuoteClientPrimeId() {
        return quoteClientPrimeId;
    }

    public void setQuoteClientPrimeId(Long quoteClientPrimeId) {
        this.quoteClientPrimeId = quoteClientPrimeId;
    }

    public QuoteStatus getQuoteStatus() {
        return quoteStatus;
    }

    public void setQuoteStatus(QuoteStatus quoteStatus) {
        this.quoteStatus = quoteStatus;
    }
}