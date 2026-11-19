package com.siec_acc.service;

import com.siec_acc.dto.response.CrmDtos;
import com.siec_acc.entity.CrmLeadEntity;

/** Activity log (lead + deal timeline). Implementation: impl/CrmActivityServiceImpl. */
public interface CrmActivityService {

    static String companyOrName(CrmLeadEntity l) {
        if (l.getCompany() != null && !l.getCompany().isBlank()) return l.getCompany();
        return ((l.getFirstName() == null ? "" : l.getFirstName()) + " " + (l.getLastName() == null ? "" : l.getLastName())).trim();
    }

    /** joins the caller's transaction, so nothing is logged for a rolled-back change */
    void log(String leadNo, String company, String type, String ref, String action, String details);

    void deleteForLead(String leadNo);

    CrmDtos.PageResponse<CrmDtos.ActivityResponse> search(String type, String action, String q, int page, int size);

    CrmDtos.ActivityStats stats();
}