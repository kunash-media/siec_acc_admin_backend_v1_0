package com.siec_acc.service;

import com.siec_acc.dto.stats.ReportDto;

public interface ReportService {
    ReportDto.Report sales(String from, String to, String groupBy, String page, String size);
    ReportDto.Report purchases(String from, String to, String groupBy, String page, String size);
    ReportDto.Report stock(String groupBy, String page, String size);
    ReportDto.Report receivables(String from, String to, String groupBy, String page, String size);
    ReportDto.Report payables(String from, String to, String groupBy, String page, String size);


    ReportDto.Report projects(String from, String to, String page, String size);
    ReportDto.Report gst(String from, String to);
    ReportDto.Report accounting(String from, String to);


}