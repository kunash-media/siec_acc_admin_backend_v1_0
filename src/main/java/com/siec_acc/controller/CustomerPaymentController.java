package com.siec_acc.controller;

import com.siec_acc.dto.request.CustomerPaymentRequestDTO;
import com.siec_acc.dto.response.CustomerPaymentResponseDTO;
import com.siec_acc.dto.response.CustomerPaymentSummaryDTO;
import com.siec_acc.service.CustomerPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-payments")
public class CustomerPaymentController {
    private static final Logger logger = LoggerFactory.getLogger(CustomerPaymentController.class);
    private final CustomerPaymentService customerPaymentService;

    public CustomerPaymentController(CustomerPaymentService customerPaymentService) {
        this.customerPaymentService = customerPaymentService;
    }

    @PostMapping
    public ResponseEntity<CustomerPaymentResponseDTO> createPayment(@RequestBody CustomerPaymentRequestDTO request) {
        logger.info("POST /api/customer-payments");
        return ResponseEntity.status(HttpStatus.CREATED).body(customerPaymentService.createPayment(request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerPaymentResponseDTO>> getAllPayments() {
        return ResponseEntity.ok(customerPaymentService.getAllPayments());
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<CustomerPaymentResponseDTO>> getPaymentsByOrganization(@PathVariable String organizationId) {
        return ResponseEntity.ok(customerPaymentService.getPaymentsByOrganization(organizationId));
    }

    @GetMapping("/organization/{organizationId}/summary")
    public ResponseEntity<List<CustomerPaymentSummaryDTO>> getCustomerPaymentSummary(@PathVariable String organizationId) {
        return ResponseEntity.ok(customerPaymentService.getCustomerPaymentSummary(organizationId));
    }

    @GetMapping("/organization/{organizationId}/customer/{clientStrId}")
    public ResponseEntity<List<CustomerPaymentResponseDTO>> getPaymentsByCustomer(
            @PathVariable String organizationId, @PathVariable String clientStrId) {
        return ResponseEntity.ok(customerPaymentService.getPaymentsByCustomer(organizationId, clientStrId));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<CustomerPaymentResponseDTO> getPayment(@PathVariable String paymentId) {
        return ResponseEntity.ok(customerPaymentService.getPayment(paymentId));
    }

    @PutMapping("/{paymentId}")
    public ResponseEntity<CustomerPaymentResponseDTO> updatePayment(
            @PathVariable String paymentId, @RequestBody CustomerPaymentRequestDTO request) {
        return ResponseEntity.ok(customerPaymentService.updatePayment(paymentId, request));
    }

    @PatchMapping("/{paymentId}")
    public ResponseEntity<CustomerPaymentResponseDTO> patchPayment(
            @PathVariable String paymentId, @RequestBody CustomerPaymentRequestDTO request) {
        return ResponseEntity.ok(customerPaymentService.patchPayment(paymentId, request));
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<String> deletePayment(@PathVariable String paymentId) {
        customerPaymentService.deletePayment(paymentId);
        return ResponseEntity.ok("Customer payment deleted successfully");
    }
}