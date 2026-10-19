package com.siec_acc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
public class QuoteEmailRequestDto {

    @NotNull(message = "Quotation id is required")
    private Long quotePrimeId;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Please enter a valid email address")
    private String toEmail;

    public QuoteEmailRequestDto(Long quotePrimeId, String toEmail) {
        this.quotePrimeId = quotePrimeId;
        this.toEmail = toEmail;
    }

    public QuoteEmailRequestDto(){}

    public Long getQuotePrimeId() {
        return quotePrimeId;
    }

    public void setQuotePrimeId(Long quotePrimeId) {
        this.quotePrimeId = quotePrimeId;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
}