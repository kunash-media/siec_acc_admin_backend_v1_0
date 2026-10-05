package com.siec_acc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
/**
 * Payload for "Send > Email" on a Proforma row.
 */
@Data
@Builder
public class ProformaEmailRequestDto {

    @NotNull(message = "proformaPrimeId is required")
    private Long proformaPrimeId;

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be a valid email address")
    private String toEmail;

    public ProformaEmailRequestDto(Long proformaPrimeId, String toEmail) {
        this.proformaPrimeId = proformaPrimeId;
        this.toEmail = toEmail;
    }

    public ProformaEmailRequestDto(){}

    public Long getProformaPrimeId() {
        return proformaPrimeId;
    }

    public void setProformaPrimeId(Long proformaPrimeId) {
        this.proformaPrimeId = proformaPrimeId;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }
}
