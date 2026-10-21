package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.request.*;
import com.siec_acc.dto.response.EmailResponseDto;
import com.siec_acc.entity.DeliveryChallanEntity;
import com.siec_acc.entity.InvoiceEntity;
import com.siec_acc.entity.ProformaEntity;
import com.siec_acc.entity.QuoteEntity;
import com.siec_acc.enum_status.EmailSendStatus;
import com.siec_acc.exceptions.*;
import com.siec_acc.repository.DeliveryChallanRepository;
import com.siec_acc.repository.InvoiceRepository;
import com.siec_acc.repository.ProformaRepository;
import com.siec_acc.repository.QuoteRepository;
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
    private final QuoteRepository quoteRepository;
    private final DeliveryChallanRepository dcRepository;

    public EmailServiceImpl(JavaMailSender mailSender, InvoiceRepository invoiceRepository, ProformaRepository proformaRepository, QuoteRepository quoteRepository, DeliveryChallanRepository dcRepository) {
        this.mailSender = mailSender;
        this.invoiceRepository = invoiceRepository;
        this.proformaRepository = proformaRepository;
        this.quoteRepository = quoteRepository;
        this.dcRepository = dcRepository;
    }

    @Value("${app.mail.from:no-reply@siec}")
    private String fromAddress;

    @Value("${app.mail.company-name:SIEC}")
    private String companyName;

    // Public base URL used to build the clickable PDF link in emails.
    @Value("${app.public.base-url:http://localhost:9093}")
    private String publicBaseUrl;

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

        String pdfLink = buildPublicUrl(invoice.getInvoicePdfUrl());

        String subject = "Invoice " + invoice.getInvoiceStrId() + " from " + companyName;
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(safe(invoice.getInvoiceCustomerName())).append(",\n\n")
                .append("Please find your invoice ").append(invoice.getInvoiceStrId())
                .append(" dated ").append(invoice.getInvoiceDate())
                .append(" for an amount of ").append(invoice.getInvoiceCurrency())
                .append(" ").append(invoice.getInvoiceTotalAmount()).append(".\n")
                .append("Due date: ").append(invoice.getInvoiceDueDate()).append("\n\n");
        if (pdfLink != null) {
            body.append("View / download your invoice (PDF):\n").append(pdfLink).append("\n\n");
        }
        body.append("Regards,\n").append(companyName);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(body.toString())
                .html(false)
                .attachmentUrl(pdfLink)
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

        String pdfLink = buildPublicUrl(proforma.getProformaPdfUrl());

        String subject = "Proforma Invoice " + proforma.getProformaStrId() + " from " + companyName;
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(safe(proforma.getProformaCustomerName())).append(",\n\n")
                .append("Please find your proforma invoice ").append(proforma.getProformaStrId())
                .append(" dated ").append(proforma.getProformaDate())
                .append(" for an amount of ").append(proforma.getProformaCurrency())
                .append(" ").append(proforma.getProformaTotalAmount()).append(".\n")
                .append("Valid until: ").append(proforma.getProformaValidUntil()).append("\n\n");
        if (pdfLink != null) {
            body.append("View / download your proforma invoice (PDF):\n").append(pdfLink).append("\n\n");
        }
        body.append("Regards,\n").append(companyName);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(body.toString())
                .html(false)
                .attachmentUrl(pdfLink)
                .attachmentFileName(proforma.getProformaStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }

    // ---------------------------------------------------------------
    // DISPATCH
    // ---------------------------------------------------------------
    private void dispatch(EmailRequestDto requestDto) {
        // Plain-text send; the PDF is delivered as a link inside the body.
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

    // ---------------------------------------------------------------
    // QUOTATION-SPECIFIC
    // ---------------------------------------------------------------
    @Override
    public EmailResponseDto sendQuoteEmail(QuoteEmailRequestDto requestDto) {
        QuoteEntity quote = quoteRepository.findById(requestDto.getQuotePrimeId())
                .orElseThrow(() -> new QuoteNotFoundException(
                        "Quotation not found with id: " + requestDto.getQuotePrimeId()));

        String pdfLink = buildPublicUrl(quote.getQuotePdfUrl());

        String clientName = quote.getQuoteClientCompanyName() != null
                ? quote.getQuoteClientCompanyName() : quote.getQuoteClientName();

        String subject = "Quotation " + quote.getQuoteStrId() + " from " + companyName;
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(safe(clientName)).append(",\n\n")
                .append("Please find our quotation ").append(quote.getQuoteStrId())
                .append(" dated ").append(quote.getQuoteDate())
                .append(" for an amount of ").append(quote.getQuoteCurrency())
                .append(" ").append(quote.getQuoteTotalAmount()).append(".\n")
                .append("Valid till: ").append(quote.getQuoteValidTill()).append("\n\n");
        if (pdfLink != null) {
            body.append("View / download your quotation (PDF):\n").append(pdfLink).append("\n\n");
        }
        body.append("Regards,\n").append(companyName);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(body.toString())
                .html(false)
                .attachmentUrl(pdfLink)
                .attachmentFileName(quote.getQuoteStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }

    // ---------------------------------------------------------------
    // DISPATCH
    // ---------------------------------------------------------------

    // ---------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------
    // baseUrl + stored pdfUrl. Returns null when no PDF url is stored yet,
    // and leaves already-absolute URLs untouched.
    private String buildPublicUrl(String pdfUrl) {
        if (pdfUrl == null || pdfUrl.isBlank()) {
            return null;
        }
        if (pdfUrl.startsWith("http://") || pdfUrl.startsWith("https://")) {
            return pdfUrl;
        }
        String base = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;
        String path = pdfUrl.startsWith("/") ? pdfUrl : "/" + pdfUrl;
        return base + path;
    }

    private String safe(String value) {
        return value == null ? "Customer" : value;
    }


    // ---------------------------------------------------------------
    // DELIVERY CHALLAN-SPECIFIC
    // ---------------------------------------------------------------
    @Override
    public EmailResponseDto sendDcEmail(DcEmailRequestDto requestDto) {
        DeliveryChallanEntity dc = dcRepository.findById(requestDto.getDcPrimeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery challan not found with id: " + requestDto.getDcPrimeId()));

        String pdfLink = buildPublicUrl(dc.getDcPdfUrl());
        String clientName = dc.getDcClientCompanyName() != null ? dc.getDcClientCompanyName() : dc.getDcClientName();

        String subject = "Delivery Challan " + dc.getDcStrId() + " from " + companyName;
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(safe(clientName)).append(",\n\n")
                .append("Please find our delivery challan ").append(dc.getDcStrId())
                .append(" dated ").append(dc.getDcDate())
                .append(" for an amount of INR ").append(dc.getDcTotalAmount()).append(".\n\n");
        if (pdfLink != null) {
            body.append("View / download your delivery challan (PDF):\n").append(pdfLink).append("\n\n");
        }
        body.append("Regards,\n").append(companyName);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(body.toString())
                .html(false)
                .attachmentUrl(pdfLink)
                .attachmentFileName(dc.getDcStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }
}
