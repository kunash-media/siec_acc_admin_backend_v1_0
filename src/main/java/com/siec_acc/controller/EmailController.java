package com.siec_acc.controller;

import com.siec_acc.dto.request.EmailRequestDto;
import com.siec_acc.dto.request.InvoiceEmailRequestDto;
import com.siec_acc.dto.request.ProformaEmailRequestDto;
import com.siec_acc.dto.response.EmailResponseDto;
import com.siec_acc.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Standalone email module, kept deliberately separate from
 * InvoiceController / ProformaController so any other module (HRMS, CRM,
 * etc.) can call /api/v1/emails/send directly instead of duplicating mail
 * logic. Add one endpoint here per module-specific "send X email" flow —
 * the invoice/proforma ones below are the pattern to follow.
 */
@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    // Generic — usable by any module.
    @PostMapping("/send")
    public ResponseEntity<EmailResponseDto> sendEmail(@Valid @RequestBody EmailRequestDto requestDto) {
        return ResponseEntity.ok(emailService.sendEmail(requestDto));
    }

    // Mirrors the frontend's Send > Email option on the Invoices tab.
    @PostMapping("/send-invoice")
    public ResponseEntity<EmailResponseDto> sendInvoiceEmail(@Valid @RequestBody InvoiceEmailRequestDto requestDto) {
        return ResponseEntity.ok(emailService.sendInvoiceEmail(requestDto));
    }

    // Mirrors the frontend's Send > Email option on the Proforma tab.
    @PostMapping("/send-proforma")
    public ResponseEntity<EmailResponseDto> sendProformaEmail(@Valid @RequestBody ProformaEmailRequestDto requestDto) {
        return ResponseEntity.ok(emailService.sendProformaEmail(requestDto));
    }
}
