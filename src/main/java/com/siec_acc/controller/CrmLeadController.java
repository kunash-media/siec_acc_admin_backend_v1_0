package com.siec_acc.controller;


import com.siec_acc.dto.response.CrmDtos;
import com.siec_acc.dto.response.CrmLeadResponseDto;
import com.siec_acc.service.CrmLeadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Pipeline views of leads used by the CRM page (Lead Registration / Contacted Leads / Dashboard). */
@RestController
@RequestMapping("/api/crm/v1/leads")
public class CrmLeadController {

    private final CrmLeadService leadService;

    public CrmLeadController(CrmLeadService leadService) { this.leadService = leadService; }

    /** stage = registered | contacted | converted | lost | all */
    @GetMapping
    public ResponseEntity<CrmDtos.PageResponse<CrmLeadResponseDto>> list(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(CrmDtos.PageResponse.of(leadService.searchPipeline(stage, source, q, page, size)));
    }

    @GetMapping("/counts")
    public ResponseEntity<Map<String, Long>> counts() { return ResponseEntity.ok(leadService.pipelineCounts()); }

    @GetMapping("/pending-followups")
    public ResponseEntity<List<CrmLeadResponseDto>> pendingFollowups() {
        return ResponseEntity.ok(leadService.getPendingFollowups());
    }

    @PatchMapping("/{leadPrimeId}/contacted")
    public ResponseEntity<CrmLeadResponseDto> contacted(@PathVariable Long leadPrimeId, @Valid @RequestBody CrmDtos.ContactedRequest req) {
        return ResponseEntity.ok(leadService.markContacted(leadPrimeId, req.note().trim(), req.nextFollowup()));
    }
}