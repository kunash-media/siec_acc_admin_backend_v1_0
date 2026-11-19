package com.siec_acc.controller;

import com.siec_acc.dto.stats.ReportDto;
import com.siec_acc.exceptions.ApiResponse;
import com.siec_acc.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/sales")
    public ResponseEntity<ApiResponse<ReportDto.Report>> sales(
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String page, @RequestParam(required = false) String size) {
        return ResponseEntity.ok(ApiResponse.success("Sales report fetched.", reportService.sales(from, to, groupBy, page, size)));
    }

    @GetMapping("/purchases")
    public ResponseEntity<ApiResponse<ReportDto.Report>> purchases(
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String page, @RequestParam(required = false) String size) {
        return ResponseEntity.ok(ApiResponse.success("Purchase report fetched.", reportService.purchases(from, to, groupBy, page, size)));
    }

    @GetMapping("/stock")
    public ResponseEntity<ApiResponse<ReportDto.Report>> stock(
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String page, @RequestParam(required = false) String size) {
        return ResponseEntity.ok(ApiResponse.success("Stock report fetched.", reportService.stock(groupBy, page, size)));
    }

    @GetMapping("/receivables")
    public ResponseEntity<ApiResponse<ReportDto.Report>> receivables(
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String page, @RequestParam(required = false) String size) {
        return ResponseEntity.ok(ApiResponse.success("Receivable report fetched.", reportService.receivables(from, to, groupBy, page, size)));
    }

    @GetMapping("/payables")
    public ResponseEntity<ApiResponse<ReportDto.Report>> payables(
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String groupBy,
            @RequestParam(required = false) String page, @RequestParam(required = false) String size) {
        return ResponseEntity.ok(ApiResponse.success("Payable report fetched.", reportService.payables(from, to, groupBy, page, size)));
    }

    @GetMapping("/projects")
    public ResponseEntity<ApiResponse<ReportDto.Report>> projects(
            @RequestParam(required = false) String from, @RequestParam(required = false) String to,
            @RequestParam(required = false) String page, @RequestParam(required = false) String size) {
        return ResponseEntity.ok(ApiResponse.success("Project report fetched.", reportService.projects(from, to, page, size)));
    }

    @GetMapping("/gst")
    public ResponseEntity<ApiResponse<ReportDto.Report>> gst(
            @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        return ResponseEntity.ok(ApiResponse.success("GST report fetched.", reportService.gst(from, to)));
    }

    @GetMapping("/accounting")
    public ResponseEntity<ApiResponse<ReportDto.Report>> accounting(
            @RequestParam(required = false) String from, @RequestParam(required = false) String to) {
        return ResponseEntity.ok(ApiResponse.success("Accounting report fetched.", reportService.accounting(from, to)));
    }
}