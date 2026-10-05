package com.siec_acc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for "Send > Email" on an Invoice row.
 * Frontend pre-fills toEmail from the customer record but lets the
 * user edit it before confirming — so it's always required here too.
 */
@Data
@Builder
public class InvoiceEmailRequestDto {

    @NotNull(message = "invoicePrimeId is required")
    private Long invoicePrimeId;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be a valid email address")
    private String toEmail;

    public InvoiceEmailRequestDto(Long invoicePrimeId, String toEmail) {
        this.invoicePrimeId = invoicePrimeId;
        this.toEmail = toEmail;
    }

    public InvoiceEmailRequestDto(){}

    public Long getInvoicePrimeId() {
        return invoicePrimeId;
    }

    public void setInvoicePrimeId(Long invoicePrimeId) {
        this.invoicePrimeId = invoicePrimeId;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
}
