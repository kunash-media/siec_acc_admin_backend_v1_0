package com.siec_acc.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Minimal staff record for lead assignment and "taken by". Replace with your ERP's staff/employee entity if you have one. */
@Entity
@Getter @Setter
@Table(name = "crm_staff")
public class CrmStaffEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffPrimeId;

    @Column(nullable = false) private String staffFirstName;
    private String staffLastName;
    private String staffRole;
    private String staffEmail;
    private String staffMobile;
    private Boolean active = true;
}