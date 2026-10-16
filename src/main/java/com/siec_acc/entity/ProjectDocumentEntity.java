package com.siec_acc.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/** Manually uploaded project files (contracts, site photos...). Invoice PDFs are listed automatically, not copied here. */
@Entity
@Table(name = "project_documents", indexes = @Index(name = "idx_pdoc_project", columnList = "project_str_id"))
public class ProjectDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pdoc_prime_id")
    private Long pdocPrimeId;

    @Column(name = "pdoc_str_id", unique = true, length = 30)
    private String pdocStrId;

    @Column(name = "project_str_id", nullable = false, length = 30)
    private String projectStrId;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] fileData;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public ProjectDocumentEntity() {}

    public Long getPdocPrimeId() {
        return pdocPrimeId;
    }

    public void setPdocPrimeId(Long pdocPrimeId) {
        this.pdocPrimeId = pdocPrimeId;
    }

    public String getPdocStrId() {
        return pdocStrId;
    }

    public void setPdocStrId(String pdocStrId) {
        this.pdocStrId = pdocStrId;
    }

    public String getProjectStrId() {
        return projectStrId;
    }

    public void setProjectStrId(String projectStrId) {
        this.projectStrId = projectStrId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}