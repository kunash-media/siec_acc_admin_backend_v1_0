package com.siec_acc.dto.response;

import com.siec_acc.enum_status.ProformaStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
public class ProformaSummaryResponseDto {
    private Long proformaPrimeId;
    private String proformaStrId;
    private LocalDate proformaValidUntil;
    private Long proformaCustomerId;
    private ProformaStatus proformaStatus;

    public ProformaSummaryResponseDto(Long proformaPrimeId, String proformaStrId, LocalDate proformaValidUntil, Long proformaCustomerId, ProformaStatus proformaStatus) {
        this.proformaPrimeId = proformaPrimeId;
        this.proformaStrId = proformaStrId;
        this.proformaValidUntil = proformaValidUntil;
        this.proformaCustomerId = proformaCustomerId;
        this.proformaStatus = proformaStatus;
    }

    public ProformaSummaryResponseDto(){}

    public Long getProformaPrimeId() {
        return proformaPrimeId;
    }

    public void setProformaPrimeId(Long proformaPrimeId) {
        this.proformaPrimeId = proformaPrimeId;
    }

    public String getProformaStrId() {
        return proformaStrId;
    }

    public void setProformaStrId(String proformaStrId) {
        this.proformaStrId = proformaStrId;
    }

    public LocalDate getProformaValidUntil() {
        return proformaValidUntil;
    }

    public void setProformaValidUntil(LocalDate proformaValidUntil) {
        this.proformaValidUntil = proformaValidUntil;
    }

    public Long getProformaCustomerId() {
        return proformaCustomerId;
    }

    public void setProformaCustomerId(Long proformaCustomerId) {
        this.proformaCustomerId = proformaCustomerId;
    }

    public ProformaStatus getProformaStatus() {
        return proformaStatus;
    }

    public void setProformaStatus(ProformaStatus proformaStatus) {
        this.proformaStatus = proformaStatus;
    }
}