package com.siec_acc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public class DcEmailRequestDto {
    @NotNull(message = "Delivery challan id is required.")
    private Long dcPrimeId;

    @NotBlank(message = "Recipient email is required.")
    @Email(message = "Please enter a valid email address.")
    private String toEmail;

    public DcEmailRequestDto(Long dcPrimeId, String toEmail) {
        this.dcPrimeId = dcPrimeId;
        this.toEmail = toEmail;
    }

    public DcEmailRequestDto(){}

    public Long getDcPrimeId() {
        return dcPrimeId;
    }

    public void setDcPrimeId(Long dcPrimeId) {
        this.dcPrimeId = dcPrimeId;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
}