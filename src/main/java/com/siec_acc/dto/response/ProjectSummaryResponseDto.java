package com.siec_acc.dto.response;

public class ProjectSummaryResponseDto {

    private Long projectPrimeId;
    private String projectStrId;
    private String projectCode;
    private String projectName;
    private String status;

    public ProjectSummaryResponseDto() {}

    public ProjectSummaryResponseDto(Long projectPrimeId, String projectStrId, String projectCode,
                                     String projectName, String status) {
        this.projectPrimeId = projectPrimeId;
        this.projectStrId = projectStrId;
        this.projectCode = projectCode;
        this.projectName = projectName;
        this.status = status;
    }

    public Long getProjectPrimeId() { return projectPrimeId; }
    public void setProjectPrimeId(Long projectPrimeId) { this.projectPrimeId = projectPrimeId; }

    public String getProjectStrId() { return projectStrId; }
    public void setProjectStrId(String projectStrId) { this.projectStrId = projectStrId; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}