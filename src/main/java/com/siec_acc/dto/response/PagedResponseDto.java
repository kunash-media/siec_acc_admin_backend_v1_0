package com.siec_acc.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Generic page wrapper — backs the frontend's page-size dropdown +
 * Prev/Next controls on the Invoices/Proforma tables.
 */
@Data
@Builder
public class PagedResponseDto<T> {
    private List<T> content;
    private int pageNumber;   // 1-based, matches frontend currentPage
    private int pageSize;
    private long totalElements;
    private int totalPages;

    public PagedResponseDto(){}

    public PagedResponseDto(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
