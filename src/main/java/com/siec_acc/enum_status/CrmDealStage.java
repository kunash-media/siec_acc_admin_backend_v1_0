package com.siec_acc.enum_status;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

public enum CrmDealStage {
    QUALIFICATION("Qualification", 10),
    SITE_VISIT("Site Visit", 30),
    PROPOSAL("Proposal", 50),
    NEGOTIATION("Negotiation", 75),
    CLOSED_WON("Closed Won", 100),
    CLOSED_LOST("Closed Lost", 0);

    private final String label;
    private final int probability;

    CrmDealStage(String label, int probability) { this.label = label; this.probability = probability; }

    public String getLabel() { return label; }
    public int getProbability() { return probability; }
    public boolean isOpen() { return this != CLOSED_WON && this != CLOSED_LOST; }

    public static List<CrmDealStage> openStages() {
        return Arrays.stream(values()).filter(CrmDealStage::isOpen).toList();
    }

    public static CrmDealStage fromLabel(String v) {
        if (v != null) {
            String t = v.trim();
            for (CrmDealStage s : values()) {
                if (s.label.equalsIgnoreCase(t) || s.name().equalsIgnoreCase(t)) return s;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown deal stage: " + v);
    }
}