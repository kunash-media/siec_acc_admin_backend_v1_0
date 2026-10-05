package com.siec_acc.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
/**
 * Generic email payload — reusable across modules (Invoice, HRMS, CRM, etc.)
 * via EmailService#sendEmail. Keep this one generic; invoice-specific
 * helper methods live in EmailService instead of new DTOs.
 */
@Data
@Builder
public class EmailRequestDto {

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Recipient email must be a valid email address")
    private String toEmail;

    private String ccEmail;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    // true = body is HTML, false = plain text
    private boolean html;

    // Optional single attachment (already-stored file, e.g. the invoice PDF).
    private String attachmentUrl;
    private String attachmentFileName;


    public EmailRequestDto(){}

    public EmailRequestDto(String toEmail, String ccEmail, String subject, String body, boolean html, String attachmentUrl, String attachmentFileName) {
        this.toEmail = toEmail;
        this.ccEmail = ccEmail;
        this.subject = subject;
        this.body = body;
        this.html = html;
        this.attachmentUrl = attachmentUrl;
        this.attachmentFileName = attachmentFileName;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getCcEmail() {
        return ccEmail;
    }

    public void setCcEmail(String ccEmail) {
        this.ccEmail = ccEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isHtml() {
        return html;
    }

    public void setHtml(boolean html) {
        this.html = html;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public void setAttachmentFileName(String attachmentFileName) {
        this.attachmentFileName = attachmentFileName;
    }
}
