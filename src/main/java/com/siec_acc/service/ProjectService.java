package com.siec_acc.service;

import com.siec_acc.dto.request.ProjectLinkRequestDto;
import com.siec_acc.dto.request.ProjectRequestDto;
import com.siec_acc.dto.response.*;
import com.siec_acc.dto.response.ProjectBudgetDto;
import com.siec_acc.dto.response.ProjectExpenseDto;
import com.siec_acc.dto.response.ProjectPurchaseDto;

import java.util.List;
import java.util.Map;

public interface ProjectService {

    ProjectResponseDto createProject(ProjectRequestDto dto);

    ProjectResponseDto updateProject(String projectStrId, ProjectRequestDto dto);

    void deleteProject(String projectStrId);

    ProjectResponseDto getProject(String projectStrId);

    /** { "projects": [...], "pagination": {...}, "meta": {...} } - page/size optional (both null = everything). */
    Map<String, Object> getAllProjects(String status, String search, Integer page, Integer size);

    /** Stat cards + dropdown options, computed over ALL projects. */
    Map<String, Object> getMeta();

    List<ProjectSaleDto> getSales(String projectStrId);

    List<ProjectPurchaseDto> getPurchases(String projectStrId);

    List<ProjectExpenseDto> getExpenses(String projectStrId);

    ProjectBudgetDto getBudget(String projectStrId);

    List<ProjectDocumentDto> getDocuments(String projectStrId);

    void linkRecord(String projectStrId, ProjectLinkRequestDto dto);

    void unlinkRecord(String projectStrId, String linkType, String refStrId);

    ProjectDocumentDto uploadDocument(String projectStrId, String documentType, String uploadedBy,
                                      byte[] data, String contentType, String fileName);

    FileDownloadDto downloadDocument(String pdocStrId);

    void deleteDocument(String pdocStrId);

    byte[] exportCsv();
}