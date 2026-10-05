package com.siec_acc.dto.response;

import java.util.List;
public class ClientListPageDto {

    private List<ClientListDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private Integer nextPage; // null when there is no next page

    public ClientListPageDto() {
    }

    public ClientListPageDto(List<ClientListDto> content, int page, int size,
                             long totalElements, int totalPages, boolean hasNext) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.nextPage = hasNext ? page + 1 : null;
    }

    public List<ClientListDto> getContent() { return content; }
    public void setContent(List<ClientListDto> content) { this.content = content; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public Integer getNextPage() { return nextPage; }
    public void setNextPage(Integer nextPage) { this.nextPage = nextPage; }
}