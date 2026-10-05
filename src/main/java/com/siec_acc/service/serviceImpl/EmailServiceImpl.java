package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.EmailRequestDto;
import com.siec_acc.dto.request.InvoiceEmailRequestDto;
import com.siec_acc.dto.request.ProformaEmailRequestDto;
import com.siec_acc.dto.response.EmailResponseDto;
import com.siec_acc.entity.InvoiceEntity;
import com.siec_acc.entity.ProformaEntity;
import com.siec_acc.enum_status.EmailSendStatus;
import com.siec_acc.exceptions.EmailSendException;
import com.siec_acc.exceptions.InvoiceNotFoundException;
import com.siec_acc.exceptions.ProformaNotFoundException;
import com.siec_acc.repository.InvoiceRepository;
import com.siec_acc.repository.ProformaRepository;
import com.siec_acc.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Standalone email module — Invoice/Proforma controllers call
 * sendInvoiceEmail / sendProformaEmail; any other module (HRMS, CRM, etc.)
 * can call the generic sendEmail directly or add its own dedicated
 * helper method here alongside these two.
 *
 * NOTE: wired to Spring's JavaMailSender / SMTP as a placeholder.
 * Swap the body of #dispatch() for the real provider (SendGrid, SES, etc.)
 * once available — nothing outside this class needs to change.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final InvoiceRepository invoiceRepository;
    private final ProformaRepository proformaRepository;

    public EmailServiceImpl(JavaMailSender mailSender, InvoiceRepository invoiceRepository, ProformaRepository proformaRepository) {
        this.mailSender = mailSender;
        this.invoiceRepository = invoiceRepository;
        this.proformaRepository = proformaRepository;
    }

    @Value("${app.mail.from:no-reply@siec}")
    private String fromAddress;

    @Value("${app.mail.company-name:SIEC}")
    private String companyName;

    // ---------------------------------------------------------------
    // GENERIC SEND
    // ---------------------------------------------------------------
    @Override
    public EmailResponseDto sendEmail(EmailRequestDto requestDto) {
        try {
            dispatch(requestDto);
            return EmailResponseDto.builder()
                    .status(EmailSendStatus.SENT)
                    .toEmail(requestDto.getToEmail())
                    .subject(requestDto.getSubject())
                    .message("Email sent successfully")
                    .sentAt(LocalDateTime.now())
                    .build();
        } catch (Exception ex) {
            log.error("Failed to send email to {}", requestDto.getToEmail(), ex);
            throw new EmailSendException("Failed to send email to " + requestDto.getToEmail(), ex);
        }
    }

    // ---------------------------------------------------------------
    // INVOICE-SPECIFIC
    // ---------------------------------------------------------------
    @Override
    public EmailResponseDto sendInvoiceEmail(InvoiceEmailRequestDto requestDto) {
        InvoiceEntity invoice = invoiceRepository.findById(requestDto.getInvoicePrimeId())
                .orElseThrow(() -> new InvoiceNotFoundException(
                        "Invoice not found with id: " + requestDto.getInvoicePrimeId()));

        String subject = "Invoice " + invoice.getInvoiceStrId() + " from " + companyName;
        String body = "Dear " + safe(invoice.getInvoiceCustomerName()) + ",\n\n"
                + "Please find attached your invoice " + invoice.getInvoiceStrId()
                + " dated " + invoice.getInvoiceDate()
                + " for an amount of " + invoice.getInvoiceCurrency() + " " + invoice.getInvoiceTotalAmount() + ".\n"
                + "Due date: " + invoice.getInvoiceDueDate() + "\n\n"
                + "Regards,\n" + companyName;

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(body)
                .html(false)
                .attachmentUrl(invoice.getInvoicePdfUrl())
                .attachmentFileName(invoice.getInvoiceStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }

    // ---------------------------------------------------------------
    // PROFORMA-SPECIFIC
    // ---------------------------------------------------------------
    @Override
    public EmailResponseDto sendProformaEmail(ProformaEmailRequestDto requestDto) {
        ProformaEntity proforma = proformaRepository.findById(requestDto.getProformaPrimeId())
                .orElseThrow(() -> new ProformaNotFoundException(
                        "Proforma invoice not found with id: " + requestDto.getProformaPrimeId()));

        String subject = "Proforma Invoice " + proforma.getProformaStrId() + " from " + companyName;
        String body = "Dear " + safe(proforma.getProformaCustomerName()) + ",\n\n"
                + "Please find attached your proforma invoice " + proforma.getProformaStrId()
                + " dated " + proforma.getProformaDate()
                + " for an amount of " + proforma.getProformaCurrency() + " " + proforma.getProformaTotalAmount() + ".\n"
                + "Valid until: " + proforma.getProformaValidUntil() + "\n\n"
                + "Regards,\n" + companyName;

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(body)
                .html(false)
                .attachmentUrl(proforma.getProformaPdfUrl())
                .attachmentFileName(proforma.getProformaStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }

    // ---------------------------------------------------------------
    // DISPATCH
    // ---------------------------------------------------------------
    private void dispatch(EmailRequestDto requestDto) {
        // Placeholder simple-text send. Swap for MimeMessageHelper +
        // attachment-by-URL once the real PDF storage / provider API is ready.
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(requestDto.getToEmail());
        if (requestDto.getCcEmail() != null && !requestDto.getCcEmail().isBlank()) {
            message.setCc(requestDto.getCcEmail());
        }
        message.setSubject(requestDto.getSubject());
        message.setText(requestDto.getBody());
        mailSender.send(message);
    }

    private String safe(String value) {
        return value == null ? "Customer" : value;
    }
}
