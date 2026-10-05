package com.siec_acc.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Carries a stored PDF (LONGBLOB) out of the service layer so the
 * controller can stream it back with the right Content-Type/filename.
 * Used by both Invoice and Proforma download endpoints.
 */
@Data
@Builder
public class FileDownloadDto {
    private byte[] data;
    private String contentType;
    private String fileName;

    public FileDownloadDto(byte[] data, String contentType, String fileName) {
        this.data = data;
        this.contentType = contentType;
        this.fileName = fileName;
    }

    public FileDownloadDto(){}

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
