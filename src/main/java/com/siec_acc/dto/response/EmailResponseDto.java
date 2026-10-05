package com.siec_acc.dto.response;

import com.siec_acc.enum_status.EmailSendStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EmailResponseDto {
    private EmailSendStatus status;
    private String toEmail;
    private String subject;
    private String message;
    private LocalDateTime sentAt;

    public EmailResponseDto(){}

    public EmailResponseDto(EmailSendStatus status, String toEmail, String subject, String message, LocalDateTime sentAt) {
        this.status = status;
        this.toEmail = toEmail;
        this.subject = subject;
        this.message = message;
        this.sentAt = sentAt;
    }

    public EmailSendStatus getStatus() {
        return status;
    }

    public void setStatus(EmailSendStatus status) {
        this.status = status;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
