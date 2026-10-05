package com.siec_acc.service;

import com.siec_acc.dto.request.EmailRequestDto;
import com.siec_acc.dto.request.InvoiceEmailRequestDto;
import com.siec_acc.dto.request.ProformaEmailRequestDto;
import com.siec_acc.dto.response.EmailResponseDto;

/**
 * Standalone, module-agnostic mail service. Keep #sendEmail() generic so
 * any other module (HRMS, CRM, etc.) can reuse it directly. Add one
 * dedicated helper method per module here (e.g. sendInvoiceEmail,
 * sendProformaEmail) instead of spreading email logic across controllers —
 * that keeps every "email this document" flow in one place going forward.
 */
public interface EmailService {

    /**
     * Generic send — any module can call this directly with a fully-built EmailRequestDto.
     */
    EmailResponseDto sendEmail(EmailRequestDto requestDto);

    /**
     * Builds the subject/body/attachment for a TAX INVOICE and sends it.
     * Mirrors the frontend's "Send > Email" flow on the Invoices tab.
     */
    EmailResponseDto sendInvoiceEmail(InvoiceEmailRequestDto requestDto);

    /**
     * Builds the subject/body/attachment for a PROFORMA INVOICE and sends it.
     * Mirrors the frontend's "Send > Email" flow on the Proforma tab.
     */
    EmailResponseDto sendProformaEmail(ProformaEmailRequestDto requestDto);
}
