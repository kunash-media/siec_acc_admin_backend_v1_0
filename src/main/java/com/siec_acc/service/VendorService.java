package com.siec_acc.service;

import com.siec_acc.dto.request.VendorRequestDto;
import com.siec_acc.dto.response.VendorListDto;
import com.siec_acc.dto.response.VendorResponseDto;
import com.siec_acc.dto.response.VendorResponseDto.PagedResponseDto;

import java.util.List;

public interface VendorService {

    VendorResponseDto createVendorWithDocuments(VendorRequestDto dto, java.util.List<org.springframework.web.multipart.MultipartFile> documents);
    VendorResponseDto getVendorByVendorId(String vendorId);
    PagedResponseDto<VendorResponseDto> getAllVendors(int page, int size, String sortBy, String sortDir);
    VendorResponseDto updateVendorWithDocuments(String vendorId, VendorRequestDto dto, java.util.List<org.springframework.web.multipart.MultipartFile> documents);
    VendorResponseDto patchVendor(String vendorId, VendorRequestDto dto, java.util.List<org.springframework.web.multipart.MultipartFile> documents);
    void deleteVendor(String vendorId);
    DocumentContent getVendorDocument(String vendorId, Long documentId);
    void deleteVendorDocument(String vendorId, Long documentId);
    List<VendorListDto> getAllVendorList();

    class DocumentContent {
        private final byte[] data;
        private final String contentType;
        private final String fileName;

        public DocumentContent(byte[] data, String contentType, String fileName) {
            this.data = data;
            this.contentType = contentType;
            this.fileName = fileName;
        }
        public byte[] getData() { return data; }
        public String getContentType() { return contentType; }
        public String getFileName() { return fileName; }
    }
}
