package com.siec_acc.dto.response;


public record CrmMonthlyLeadCountDto(String monthKey, String monthLabel, int year, long count) {}