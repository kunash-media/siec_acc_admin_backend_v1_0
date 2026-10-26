package com.siec_acc.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class AdminResponseDto {

    private String adminId;
    private String adminFirstName;
    private String adminLastName;
    private String adminMobileNumber;
    private String adminAddress;
    private String adminEmail;
    private String adminRole;
    private String adminDepartment;
    private Boolean adminIsActive;
    private Boolean adminIsLocked;
    private java.time.LocalDateTime adminLastLoginAt;
    private java.time.LocalDateTime adminCreatedAt;


    public AdminResponseDto() {}

    public AdminResponseDto(String adminId, String adminFirstName, String adminLastName, String adminMobileNumber, String adminAddress, String adminEmail, String adminRole, String adminDepartment, Boolean adminIsActive, Boolean adminIsLocked, LocalDateTime adminLastLoginAt, LocalDateTime adminCreatedAt) {
        this.adminId = adminId;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
        this.adminMobileNumber = adminMobileNumber;
        this.adminAddress = adminAddress;
        this.adminEmail = adminEmail;
        this.adminRole = adminRole;
        this.adminDepartment = adminDepartment;
        this.adminIsActive = adminIsActive;
        this.adminIsLocked = adminIsLocked;
        this.adminLastLoginAt = adminLastLoginAt;
        this.adminCreatedAt = adminCreatedAt;
    }

    // Getters and Setters
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }

    public String getAdminFirstName() { return adminFirstName; }
    public void setAdminFirstName(String adminFirstName) { this.adminFirstName = adminFirstName; }

    public String getAdminLastName() { return adminLastName; }
    public void setAdminLastName(String adminLastName) { this.adminLastName = adminLastName; }

    public String getAdminMobileNumber() { return adminMobileNumber; }
    public void setAdminMobileNumber(String adminMobileNumber) { this.adminMobileNumber = adminMobileNumber; }

    public String getAdminAddress() { return adminAddress; }
    public void setAdminAddress(String adminAddress) { this.adminAddress = adminAddress; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminRole() { return adminRole; }
    public void setAdminRole(String adminRole) { this.adminRole = adminRole; }

    public String getAdminDepartment() { return adminDepartment; }
    public void setAdminDepartment(String adminDepartment) { this.adminDepartment = adminDepartment; }

    public Boolean getAdminIsActive() {
        return adminIsActive;
    }

    public void setAdminIsActive(Boolean adminIsActive) {
        this.adminIsActive = adminIsActive;
    }

    public Boolean getAdminIsLocked() {
        return adminIsLocked;
    }

    public void setAdminIsLocked(Boolean adminIsLocked) {
        this.adminIsLocked = adminIsLocked;
    }

    public LocalDateTime getAdminLastLoginAt() {
        return adminLastLoginAt;
    }

    public void setAdminLastLoginAt(LocalDateTime adminLastLoginAt) {
        this.adminLastLoginAt = adminLastLoginAt;
    }

    public LocalDateTime getAdminCreatedAt() {
        return adminCreatedAt;
    }

    public void setAdminCreatedAt(LocalDateTime adminCreatedAt) {
        this.adminCreatedAt = adminCreatedAt;
    }
}