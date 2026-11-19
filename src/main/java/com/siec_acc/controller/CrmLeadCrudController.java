package com.siec_acc.controller;

import com.siec_acc.dto.request.CrmLeadFollowupRequestDto;
import com.siec_acc.dto.request.CrmLeadOutcomeRequest;
import com.siec_acc.dto.request.CrmLeadRequestDto;
import com.siec_acc.dto.response.CrmLeadFollowupResponseDto;
import com.siec_acc.dto.response.CrmLeadResponseDto;
import com.siec_acc.dto.response.CrmMonthlyLeadCountDto;
import com.siec_acc.service.CrmLeadService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Core lead CRUD, follow-ups and outcome. Pipeline views live in CrmLeadCrudController. */
@RestController
@RequestMapping("/api/crm/v1/lead")
public class CrmLeadCrudController {

    private final CrmLeadService leadService;

    public CrmLeadCrudController(CrmLeadService leadService) { this.leadService = leadService; }

    // multipart: part "lead" = JSON (the UI sends it as a Blob)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CrmLeadResponseDto> create(@RequestPart("lead") @Valid CrmLeadRequestDto dto) {
        return ResponseEntity.ok(leadService.createLead(dto));
    }

    @PatchMapping(value = "/{leadPrimeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CrmLeadResponseDto> update(@PathVariable Long leadPrimeId, @RequestPart("lead") @Valid CrmLeadRequestDto dto) {
        return ResponseEntity.ok(leadService.updateLead(leadPrimeId, dto));
    }

    @GetMapping("/{leadPrimeId}")
    public ResponseEntity<CrmLeadResponseDto> get(@PathVariable Long leadPrimeId) {
        return ResponseEntity.ok(leadService.getLead(leadPrimeId));
    }

    @DeleteMapping("/delete-lead/{leadPrimeId}")
    public ResponseEntity<Void> delete(@PathVariable Long leadPrimeId) {
        leadService.deleteLead(leadPrimeId);
        return ResponseEntity.noContent().build();
    }

    /** 204 = phone is free, 200 = existing lead returned */
    @GetMapping("/check-phone")
    public ResponseEntity<CrmLeadResponseDto> checkPhone(@RequestParam String phone) {
        CrmLeadResponseDto existing = leadService.checkPhoneExists(phone);
        return existing == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(existing);
    }

    @PostMapping("/add/{leadPrimeId}/followup")
    public ResponseEntity<CrmLeadResponseDto> addFollowup(@PathVariable Long leadPrimeId, @RequestBody @Valid CrmLeadFollowupRequestDto dto) {
        return ResponseEntity.ok(leadService.addFollowup(leadPrimeId, dto));
    }

    @GetMapping("/history/{leadPrimeId}/followup")
    public ResponseEntity<List<CrmLeadFollowupResponseDto>> history(@PathVariable Long leadPrimeId) {
        return ResponseEntity.ok(leadService.getFollowupHistory(leadPrimeId));
    }

    @PatchMapping("/{leadPrimeId}/outcome")
    public ResponseEntity<Void> outcome(@PathVariable Long leadPrimeId, @RequestBody @Valid CrmLeadOutcomeRequest req) {
        leadService.updateLeadOutcome(leadPrimeId, req.leadOutcome(), req.lostReason());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/monthly-count")
    public ResponseEntity<List<CrmMonthlyLeadCountDto>> monthlyCount(@RequestParam(defaultValue = "6") int monthsBack) {
        return ResponseEntity.ok(leadService.getMonthlyLeadCounts(monthsBack));
    }
}