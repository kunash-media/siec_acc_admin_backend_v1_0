package com.siec_acc.controller;


import com.siec_acc.dto.response.CrmDtos;
import com.siec_acc.service.CrmDealService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/v1/deals")
public class CrmDealController {

    private final CrmDealService dealService;

    public CrmDealController(CrmDealService dealService) { this.dealService = dealService; }

    /** view = open | won | lost */
    @GetMapping
    public ResponseEntity<CrmDtos.PageResponse<CrmDtos.DealResponse>> list(
            @RequestParam(defaultValue = "open") String view,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(dealService.search(view, stage, q, page, size));
    }

    @GetMapping("/stats")
    public ResponseEntity<CrmDtos.DealStats> stats() { return ResponseEntity.ok(dealService.stats()); }

    @GetMapping("/{id}")
    public ResponseEntity<CrmDtos.DealResponse> get(@PathVariable Long id) { return ResponseEntity.ok(dealService.get(id)); }

    @PostMapping("/convert/{leadPrimeId}")
    public ResponseEntity<CrmDtos.DealResponse> convert(@PathVariable Long leadPrimeId, @Valid @RequestBody CrmDtos.ConvertDealRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealService.convertLead(leadPrimeId, req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CrmDtos.DealResponse> update(@PathVariable Long id, @Valid @RequestBody CrmDtos.DealUpdateRequest req) {
        return ResponseEntity.ok(dealService.update(id, req));
    }

    @PatchMapping("/{id}/stage")
    public ResponseEntity<CrmDtos.DealResponse> stage(@PathVariable Long id, @Valid @RequestBody CrmDtos.StageRequest req) {
        return ResponseEntity.ok(dealService.changeStage(id, req));
    }

    @PatchMapping("/{id}/won")
    public ResponseEntity<CrmDtos.DealResponse> won(@PathVariable Long id, @Valid @RequestBody CrmDtos.WonRequest req) {
        return ResponseEntity.ok(dealService.markWon(id, req));
    }

    @PatchMapping("/{id}/lost")
    public ResponseEntity<CrmDtos.DealResponse> lost(@PathVariable Long id, @Valid @RequestBody CrmDtos.LostRequest req) {
        return ResponseEntity.ok(dealService.markLost(id, req));
    }
}