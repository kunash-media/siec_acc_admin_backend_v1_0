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
import com.siec_acc.service.rbac.ShareLinkService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Standalone email module — Invoice/Proforma/Quote/DC controllers call the
 * dedicated send* methods; any other module can call the generic sendEmail.
 *
 * Emails are sent as branded HTML (logo embedded inline via CID) with an
 * auto-generated plain-text alternative.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    // Brand colours taken from the SIEC logo
    private static final String COLOR_TEAL = "#1A7690";
    private static final String COLOR_GREEN = "#6AA803";
    private static final String COLOR_GREEN_LIGHT = "#A4CD3C";
    private static final String COLOR_GREEN_MID = "#4CB247";
    private static final String COLOR_BLUE = "#0B9CD3";
    private static final String COLOR_BG = "#F2F7F5";
    private static final String COLOR_TEXT = "#2B3A42";
    private static final String COLOR_MUTED = "#6B7B83";

    private static final String LOGO_CID = "siecLogo";
    private static final ClassPathResource LOGO =
            new ClassPathResource("static/Images/siec-logo-without-bg_.png");

    private final JavaMailSender mailSender;
    private final InvoiceRepository invoiceRepository;
    private final ProformaRepository proformaRepository;
    private final QuoteRepository quoteRepository;
    private final DeliveryChallanRepository dcRepository;
    private final ShareLinkService shareLinkService;

    public EmailServiceImpl(JavaMailSender mailSender, InvoiceRepository invoiceRepository,
                            ProformaRepository proformaRepository, QuoteRepository quoteRepository,
                            DeliveryChallanRepository dcRepository, ShareLinkService shareLinkService) {
        this.shareLinkService = shareLinkService;
        this.mailSender = mailSender;
        this.invoiceRepository = invoiceRepository;
        this.proformaRepository = proformaRepository;
        this.quoteRepository = quoteRepository;
        this.dcRepository = dcRepository;
    }

    @Value("${app.mail.from:accounts@siecwater.com}")
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

        String pdfLink = signedLink(ShareLinkService.DocType.INVOICE, invoice.getInvoiceStrId(), invoice.getInvoicePdfUrl());

        String subject = "Invoice " + invoice.getInvoiceStrId() + " from " + companyName;
        String html = buildHtml(
                "Invoice " + invoice.getInvoiceStrId(),
                "Your Invoice",
                safe(invoice.getInvoiceCustomerName()),
                "Please find your invoice details below.",
                new String[][]{
                        {"Invoice No.", str(invoice.getInvoiceStrId())},
                        {"Invoice Date", str(invoice.getInvoiceDate())},
                        {"Due Date", str(invoice.getInvoiceDueDate())},
                        {"Amount", str(invoice.getInvoiceCurrency()) + " " + str(invoice.getInvoiceTotalAmount())}
                },
                "View / Download Invoice (PDF)",
                pdfLink);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(html)
                .html(true)
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

        String pdfLink = signedLink(ShareLinkService.DocType.PROFORMA, proforma.getProformaStrId(), proforma.getProformaPdfUrl());

        String subject = "Proforma Invoice " + proforma.getProformaStrId() + " from " + companyName;
        String html = buildHtml(
                "Proforma Invoice " + proforma.getProformaStrId(),
                "Your Proforma Invoice",
                safe(proforma.getProformaCustomerName()),
                "Please find your proforma invoice details below.",
                new String[][]{
                        {"Proforma No.", str(proforma.getProformaStrId())},
                        {"Date", str(proforma.getProformaDate())},
                        {"Valid Until", str(proforma.getProformaValidUntil())},
                        {"Amount", str(proforma.getProformaCurrency()) + " " + str(proforma.getProformaTotalAmount())}
                },
                "View / Download Proforma Invoice (PDF)",
                pdfLink);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(html)
                .html(true)
                .attachmentUrl(pdfLink)
                .attachmentFileName(proforma.getProformaStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }

    // ---------------------------------------------------------------
    // DISPATCH
    // ---------------------------------------------------------------
    private void dispatch(EmailRequestDto requestDto) throws Exception {
        String body = requestDto.getBody() == null ? "" : requestDto.getBody();
        boolean isHtmlBody = body.trim().regionMatches(true, 0, "<!DOCTYPE", 0, 9)
                || body.trim().regionMatches(true, 0, "<html", 0, 5);

        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, StandardCharsets.UTF_8.name());

        helper.setFrom(fromAddress, companyName);
        helper.setTo(requestDto.getToEmail());
        if (requestDto.getCcEmail() != null && !requestDto.getCcEmail().isBlank()) {
            helper.setCc(requestDto.getCcEmail());
        }
        helper.setSubject(requestDto.getSubject());

        if (isHtmlBody) {
            helper.setText(htmlToPlain(body), body);
            if (body.contains("cid:" + LOGO_CID) && LOGO.exists()) {
                helper.addInline(LOGO_CID, LOGO, "image/png");
            }
        } else {
            helper.setText(body, false);
        }

        mailSender.send(mime);
    }

    // ---------------------------------------------------------------
    // QUOTATION-SPECIFIC
    // ---------------------------------------------------------------
    @Override
    public EmailResponseDto sendQuoteEmail(QuoteEmailRequestDto requestDto) {
        QuoteEntity quote = quoteRepository.findById(requestDto.getQuotePrimeId())
                .orElseThrow(() -> new QuoteNotFoundException(
                        "Quotation not found with id: " + requestDto.getQuotePrimeId()));

        String pdfLink = signedLink(ShareLinkService.DocType.QUOTE, quote.getQuoteStrId(), quote.getQuotePdfUrl());

        String clientName = quote.getQuoteClientCompanyName() != null
                ? quote.getQuoteClientCompanyName() : quote.getQuoteClientName();

        String subject = "Quotation " + quote.getQuoteStrId() + " from " + companyName;
        String html = buildHtml(
                "Quotation " + quote.getQuoteStrId(),
                "Your Quotation",
                safe(clientName),
                "Thank you for your interest. Please find our quotation details below.",
                new String[][]{
                        {"Quotation No.", str(quote.getQuoteStrId())},
                        {"Date", str(quote.getQuoteDate())},
                        {"Valid Till", str(quote.getQuoteValidTill())},
                        {"Amount", str(quote.getQuoteCurrency()) + " " + str(quote.getQuoteTotalAmount())}
                },
                "View / Download Quotation (PDF)",
                pdfLink);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(html)
                .html(true)
                .attachmentUrl(pdfLink)
                .attachmentFileName(quote.getQuoteStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }

    // ---------------------------------------------------------------
    // DELIVERY CHALLAN-SPECIFIC
    // ---------------------------------------------------------------
    @Override
    public EmailResponseDto sendDcEmail(DcEmailRequestDto requestDto) {
        DeliveryChallanEntity dc = dcRepository.findById(requestDto.getDcPrimeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery challan not found with id: " + requestDto.getDcPrimeId()));

        String pdfLink = signedLink(ShareLinkService.DocType.DC, dc.getDcStrId(), dc.getDcPdfUrl());

        String clientName = dc.getDcClientCompanyName() != null ? dc.getDcClientCompanyName() : dc.getDcClientName();

        String subject = "Delivery Challan " + dc.getDcStrId() + " from " + companyName;
        String html = buildHtml(
                "Delivery Challan " + dc.getDcStrId(),
                "Your Delivery Challan",
                safe(clientName),
                "Please find your delivery challan details below.",
                new String[][]{
                        {"Challan No.", str(dc.getDcStrId())},
                        {"Date", str(dc.getDcDate())},
                        {"Amount", "INR " + str(dc.getDcTotalAmount())}
                },
                "View / Download Delivery Challan (PDF)",
                pdfLink);

        EmailRequestDto emailRequest = EmailRequestDto.builder()
                .toEmail(requestDto.getToEmail())
                .subject(subject)
                .body(html)
                .html(true)
                .attachmentUrl(pdfLink)
                .attachmentFileName(dc.getDcStrId().replace("/", "-") + ".pdf")
                .build();

        return sendEmail(emailRequest);
    }

    // ---------------------------------------------------------------
    // HTML TEMPLATE
    // ---------------------------------------------------------------
    private String buildHtml(String preheader, String heading, String greetingName,
                             String intro, String[][] rows, String buttonLabel, String link) {

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>").append(esc(heading)).append("</title></head>")
                .append("<body style=\"margin:0;padding:0;background-color:").append(COLOR_BG)
                .append(";font-family:Segoe UI,Helvetica,Arial,sans-serif;color:").append(COLOR_TEXT).append(";\">");

        // hidden preheader
        sb.append("<div style=\"display:none;max-height:0;overflow:hidden;opacity:0;\">")
                .append(esc(preheader)).append("</div>");

        sb.append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
                .append("style=\"background-color:").append(COLOR_BG).append(";padding:24px 12px;\"><tr><td align=\"center\">");

        sb.append("<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
                .append("style=\"width:100%;max-width:600px;background-color:#ffffff;border-radius:10px;overflow:hidden;")
                .append("box-shadow:0 2px 8px rgba(0,0,0,0.08);\">");

        // top gradient bar (same gradient as the logo frame)
        sb.append("<tr><td style=\"height:6px;line-height:6px;font-size:0;background-color:").append(COLOR_GREEN_MID)
                .append(";background-image:linear-gradient(90deg,#3C8F3A,").append(COLOR_GREEN_MID)
                .append(",").append(COLOR_GREEN_LIGHT).append(");\">&nbsp;</td></tr>");

        // logo
        sb.append("<tr><td align=\"center\" style=\"padding:26px 24px 8px 24px;\">");
        if (LOGO.exists()) {
            sb.append("<img src=\"cid:").append(LOGO_CID).append("\" alt=\"").append(esc(companyName))
                    .append("\" width=\"150\" style=\"display:block;width:150px;max-width:150px;height:auto;border:0;\">");
        } else {
            sb.append("<div style=\"font-size:28px;font-weight:700;color:").append(COLOR_TEAL).append(";\">")
                    .append(esc(companyName)).append("</div>");
        }
        sb.append("</td></tr>");

        // heading
        sb.append("<tr><td align=\"center\" style=\"padding:8px 32px 0 32px;\">")
                .append("<h1 style=\"margin:0;font-size:22px;line-height:30px;color:").append(COLOR_TEAL).append(";\">")
                .append(esc(heading)).append("</h1>")
                .append("<div style=\"width:48px;height:3px;background-color:").append(COLOR_GREEN)
                .append(";margin:12px auto 0 auto;\"></div></td></tr>");

        // greeting + intro
        sb.append("<tr><td style=\"padding:24px 32px 8px 32px;font-size:15px;line-height:24px;\">")
                .append("<p style=\"margin:0 0 12px 0;\">Dear <strong>").append(esc(greetingName)).append("</strong>,</p>")
                .append("<p style=\"margin:0;\">").append(esc(intro)).append("</p></td></tr>");

        // details table
        sb.append("<tr><td style=\"padding:16px 32px;\">")
                .append("<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" ")
                .append("style=\"border:1px solid #DDE8E4;border-radius:8px;border-collapse:separate;\">");
        for (int i = 0; i < rows.length; i++) {
            boolean last = i == rows.length - 1;
            boolean amount = "Amount".equals(rows[i][0]);
            String border = last ? "" : "border-bottom:1px solid #E6EFEB;";
            sb.append("<tr>")
                    .append("<td style=\"padding:12px 16px;font-size:13px;color:").append(COLOR_MUTED).append(";")
                    .append(border).append("width:40%;\">").append(esc(rows[i][0])).append("</td>")
                    .append("<td style=\"padding:12px 16px;font-size:").append(amount ? "17px" : "14px")
                    .append(";font-weight:").append(amount ? "700" : "600")
                    .append(";color:").append(amount ? COLOR_TEAL : COLOR_TEXT).append(";")
                    .append(border).append("\">").append(esc(rows[i][1])).append("</td></tr>");
        }
        sb.append("</table></td></tr>");

        // button
        if (link != null) {
            sb.append("<tr><td align=\"center\" style=\"padding:8px 32px 8px 32px;\">")
                    .append("<a href=\"").append(esc(link)).append("\" target=\"_blank\" ")
                    .append("style=\"display:inline-block;background-color:").append(COLOR_TEAL)
                    .append(";color:#ffffff;text-decoration:none;font-size:15px;font-weight:600;")
                    .append("padding:13px 28px;border-radius:6px;border-bottom:3px solid ").append(COLOR_GREEN).append(";\">")
                    .append(esc(buttonLabel)).append("</a></td></tr>")
                    .append("<tr><td align=\"center\" style=\"padding:10px 32px 0 32px;font-size:12px;color:")
                    .append(COLOR_MUTED).append(";line-height:18px;\">If the button does not work, copy this link into your browser:<br>")
                    .append("<a href=\"").append(esc(link)).append("\" style=\"color:").append(COLOR_BLUE)
                    .append(";word-break:break-all;\">").append(esc(link)).append("</a></td></tr>");
        }

        // closing
        sb.append("<tr><td style=\"padding:24px 32px 28px 32px;font-size:15px;line-height:24px;\">")
                .append("<p style=\"margin:0 0 4px 0;\">Regards,</p>")
                .append("<p style=\"margin:0;font-weight:700;color:").append(COLOR_TEAL).append(";\">")
                .append(esc(companyName)).append("</p></td></tr>");

        // footer
        sb.append("<tr><td align=\"center\" style=\"background-color:").append(COLOR_TEAL)
                .append(";padding:18px 24px;font-size:12px;line-height:18px;color:#D6ECF2;\">")
                .append("<strong style=\"color:#ffffff;\">Sustainable Integrated Environment Consultant</strong><br>")
                .append("This is an automated email. For queries, reply to ")
                .append("<a href=\"mailto:").append(esc(fromAddress)).append("\" style=\"color:#ffffff;text-decoration:underline;\">")
                .append("<span style=\"color:#ffffff;\">").append(esc(fromAddress)).append("</span></a>.")
                .append("</td></tr>");

        sb.append("</table></td></tr></table></body></html>");
        return sb.toString();
    }

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

    private String signedLink(ShareLinkService.DocType type, String strId, String pdfUrl) {
        if (pdfUrl == null || pdfUrl.isBlank()) return null;
        return shareLinkService.buildUrl(type, strId);
    }

    private String safe(String value) {
        return value == null ? "Customer" : value;
    }

    private String str(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }

    private String esc(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    // Crude plain-text alternative for clients/filters that want one.
    private String htmlToPlain(String html) {
        return html
                .replaceAll("(?is)<(style|head)[^>]*>.*?</\\1>", "")
                .replaceAll("(?i)</(tr|p|h1|div)>", "\n")
                .replaceAll("(?i)</td>", "  ")
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("<[^>]+>", "")
                .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                .replace("&quot;", "\"").replace("&nbsp;", " ")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("(?m)^ +| +$", "")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}