package com.siec_acc.controller;

import com.siec_acc.dto.request.VendorPaymentRequestDto;
import com.siec_acc.dto.response.VendorPaymentResponseDto;
import com.siec_acc.exceptions.ApiResponse;
import com.siec_acc.service.VendorPaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-payments/v1")
public class VendorPaymentController {

    private static final Logger logger = LoggerFactory.getLogger(VendorPaymentController.class);

    private final VendorPaymentService vendorPaymentService;

    public VendorPaymentController(VendorPaymentService vendorPaymentService) {
        this.vendorPaymentService = vendorPaymentService;
    }

    @PostMapping("/create-vendor-payment")
    public ResponseEntity<ApiResponse<VendorPaymentResponseDto>> create(@Valid @RequestBody VendorPaymentRequestDto dto) {
        logger.info("API HIT: POST /create-vendor-payment | vendorId={}", dto.getVendorId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vendor payment recorded successfully.", vendorPaymentService.createVendorPayment(dto)));
    }

    @PutMapping("/update-vendor-payment/{vpStrId}")
    public ResponseEntity<ApiResponse<VendorPaymentResponseDto>> update(
            @PathVariable String vpStrId, @Valid @RequestBody VendorPaymentRequestDto dto) {
        logger.info("API HIT: PUT /update-vendor-payment/{}", vpStrId);
        return ResponseEntity.ok(ApiResponse.success("Vendor payment updated successfully.", vendorPaymentService.updateVendorPayment(vpStrId, dto)));
    }

    @DeleteMapping("/delete-vendor-payment/{vpStrId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String vpStrId) {
        logger.info("API HIT: DELETE /delete-vendor-payment/{}", vpStrId);
        vendorPaymentService.deleteVendorPayment(vpStrId);
        return ResponseEntity.ok(ApiResponse.success("Vendor payment deleted successfully.", null));
    }

    @GetMapping("/get-vendor-payment/{vpStrId}")
    public ResponseEntity<ApiResponse<VendorPaymentResponseDto>> get(@PathVariable String vpStrId) {
        logger.info("API HIT: GET /get-vendor-payment/{}", vpStrId);
        return ResponseEntity.ok(ApiResponse.success("Vendor payment fetched successfully.", vendorPaymentService.getVendorPayment(vpStrId)));
    }

    // Optional filters: mode (e.g. UPI), vendorId (vendorStrId), search (payment #, vendor, reference).
    @GetMapping("/get-all-vendor-payments")
    public ResponseEntity<ApiResponse<List<VendorPaymentResponseDto>>> getAll(
            @RequestParam(required = false) String mode,
            @RequestParam(required = false) String vendorId,
            @RequestParam(required = false) String search) {
        logger.info("API HIT: GET /get-all-vendor-payments | mode={} vendorId={} search={}", mode, vendorId, search);
        return ResponseEntity.ok(ApiResponse.success("Vendor payments fetched successfully.",
                vendorPaymentService.getAllVendorPayments(mode, vendorId, search)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<VendorPaymentResponseDto.VendorPaymentStatsResponseDto>> stats() {
        logger.info("API HIT: GET /vendor-payments/stats");
        return ResponseEntity.ok(ApiResponse.success("Vendor payment stats fetched successfully.", vendorPaymentService.getStats()));
    }

    // Open bills of a vendor for the "Allocate to Bills" dropdown. excludeVpStrId = payment being edited.
    @GetMapping("/vendor-bills/{vendorId}")
    public ResponseEntity<ApiResponse<List<VendorPaymentResponseDto.VendorOpenBillResponseDto>>> vendorBills(
            @PathVariable String vendorId, @RequestParam(required = false) String excludeVpStrId) {
        logger.info("API HIT: GET /vendor-bills/{}", vendorId);
        return ResponseEntity.ok(ApiResponse.success("Vendor bills fetched successfully.",
                vendorPaymentService.getOpenBillsOfVendor(vendorId, excludeVpStrId)));

    }
}