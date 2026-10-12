package com.siec_acc.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "organization_document")
public class OrganizationDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_prime_id")
    private Long documentPrimeId;

    @Column(name = "document_id", unique = true, nullable = false, length = 40)
    private String documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_prime_id", nullable = false)
    private OrganizationEntity organization;

    @Column(name = "document_name", nullable = false, length = 150)
    private String documentName;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Lob
    @Column(name = "document_blob", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] documentBlob;

    @Column(name = "document_url", length = 500)
    private String documentUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public OrganizationDocumentEntity() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    public Long getDocumentPrimeId() { return documentPrimeId; }
    public void setDocumentPrimeId(Long documentPrimeId) { this.documentPrimeId = documentPrimeId; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public OrganizationEntity getOrganization() { return organization; }
    public void setOrganization(OrganizationEntity organization) { this.organization = organization; }
    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }
    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public byte[] getDocumentBlob() { return documentBlob; }
    public void setDocumentBlob(byte[] documentBlob) { this.documentBlob = documentBlob; }
    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}