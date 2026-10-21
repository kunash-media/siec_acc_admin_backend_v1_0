package com.siec_acc.dto.response;

import com.siec_acc.enum_status.DcStatus;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public class DeliveryChallanSummaryResponseDto {
    private Long dcPrimeId;
    private String dcStrId;
    private LocalDate dcDate;
    private Long dcClientPrimeId;
    private DcStatus dcStatus;

    public DeliveryChallanSummaryResponseDto(Long dcPrimeId, String dcStrId, LocalDate dcDate, Long dcClientPrimeId, DcStatus dcStatus) {
        this.dcPrimeId = dcPrimeId;
        this.dcStrId = dcStrId;
        this.dcDate = dcDate;
        this.dcClientPrimeId = dcClientPrimeId;
        this.dcStatus = dcStatus;
    }

    public DeliveryChallanSummaryResponseDto(){}


    public Long getDcPrimeId() {
        return dcPrimeId;
    }

    public void setDcPrimeId(Long dcPrimeId) {
        this.dcPrimeId = dcPrimeId;
    }

    public String getDcStrId() {
        return dcStrId;
    }

    public void setDcStrId(String dcStrId) {
        this.dcStrId = dcStrId;
    }

    public LocalDate getDcDate() {
        return dcDate;
    }

    public void setDcDate(LocalDate dcDate) {
        this.dcDate = dcDate;
    }

    public Long getDcClientPrimeId() {
        return dcClientPrimeId;
    }

    public void setDcClientPrimeId(Long dcClientPrimeId) {
        this.dcClientPrimeId = dcClientPrimeId;
    }

    public DcStatus getDcStatus() {
        return dcStatus;
    }

    public void setDcStatus(DcStatus dcStatus) {
        this.dcStatus = dcStatus;
    }
}