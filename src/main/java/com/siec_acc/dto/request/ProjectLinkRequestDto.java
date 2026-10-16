package com.siec_acc.dto.request;

public class ProjectLinkRequestDto {
    /** INVOICE | PURCHASE_BILL */
    private String linkType;
    /** invoiceStrId or pbStrId */
    private String refStrId;

    public ProjectLinkRequestDto() {}

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }

    public String getRefStrId() {
        return refStrId;
    }

    public void setRefStrId(String refStrId) {
        this.refStrId = refStrId;
    }
}