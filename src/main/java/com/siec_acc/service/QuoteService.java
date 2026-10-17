package com.siec_acc.service;

import com.siec_acc.dto.request.QuoteRequestDto;

import com.siec_acc.dto.response.FileDownloadDto;
import com.siec_acc.dto.response.PagedResponseDto;
import com.siec_acc.dto.response.QuoteResponseDto;
import com.siec_acc.dto.response.QuoteStatsResponseDto;
import com.siec_acc.enum_status.QuoteStatus;

public interface QuoteService {
    QuoteResponseDto createQuote(QuoteRequestDto requestDto, byte[] fileBytes, String contentType, String fileName);
    QuoteResponseDto updateQuote(Long quotePrimeId, QuoteRequestDto requestDto);
    QuoteResponseDto getQuoteById(Long quotePrimeId);
    PagedResponseDto<QuoteResponseDto> getAllQuotes(QuoteStatus status, Long clientPrimeId, String search, int pageNumber, int pageSize);
    void deleteQuote(Long quotePrimeId);
    QuoteStatsResponseDto getQuoteStats();
    QuoteResponseDto updateQuoteStatus(Long quotePrimeId, QuoteStatus status);
    QuoteResponseDto convertToSalesOrder(Long quotePrimeId);
    QuoteResponseDto uploadQuotePdf(Long quotePrimeId, byte[] fileBytes, String contentType, String fileName);
    FileDownloadDto downloadQuotePdf(String quoteStrId);
}