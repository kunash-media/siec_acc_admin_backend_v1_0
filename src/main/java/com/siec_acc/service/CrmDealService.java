package com.siec_acc.service;


import com.siec_acc.dto.response.CrmDtos;

/** Deal pipeline: convert from lead, stage changes, won / lost. Implementation: impl/CrmDealServiceImpl. */
public interface CrmDealService {

    CrmDtos.DealResponse convertLead(Long leadPrimeId, CrmDtos.ConvertDealRequest req);

    /** view = open | won | lost | all */
    CrmDtos.PageResponse<CrmDtos.DealResponse> search(String view, String stage, String q, int page, int size);

    CrmDtos.DealResponse get(Long id);

    CrmDtos.DealStats stats();

    CrmDtos.DealResponse update(Long id, CrmDtos.DealUpdateRequest req);

    CrmDtos.DealResponse changeStage(Long id, CrmDtos.StageRequest req);

    CrmDtos.DealResponse markWon(Long id, CrmDtos.WonRequest req);

    CrmDtos.DealResponse markLost(Long id, CrmDtos.LostRequest req);
}