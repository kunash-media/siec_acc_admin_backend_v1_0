package com.siec_acc.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDocumentDto {
    private String id;
    private String type;
    private String name;
    private LocalDate date;
    private Long size;          // bytes (null for auto-listed invoice PDFs)
    private String uploadedBy;
    private String source;      // UPLOADED | AUTO
    private String downloadUrl;

    public ProjectDocumentDto() {}

    // JPQL constructor expression (ProjectDocumentRepository).
    public ProjectDocumentDto(String id, String type, String name, Long size, String uploadedBy, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.size = size;
        this.uploadedBy = uploadedBy;
        this.date = createdAt == null ? null : createdAt.toLocalDate();
        this.source = "UPLOADED";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}