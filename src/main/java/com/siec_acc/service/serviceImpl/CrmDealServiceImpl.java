package com.siec_acc.service.serviceImpl;

import com.siec_acc.dto.response.CrmDtos;
import com.siec_acc.entity.CrmDealEntity;
import com.siec_acc.entity.CrmLeadEntity;
import com.siec_acc.enum_status.CrmDealStage;
import com.siec_acc.repository.CrmDealRepository;
import com.siec_acc.repository.CrmLeadRepository;
import com.siec_acc.service.CrmActivityService;
import com.siec_acc.service.CrmDealService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class CrmDealServiceImpl implements CrmDealService {

    private final CrmDealRepository dealRepository;
    private final CrmLeadRepository leadRepository;
    private final CrmActivityService activityService;

    public CrmDealServiceImpl(CrmDealRepository dealRepository, CrmLeadRepository leadRepository, CrmActivityService activityService) {
        this.dealRepository = dealRepository;
        this.leadRepository = leadRepository;
        this.activityService = activityService;
    }

    // ---------------- create (lead -> deal) ----------------
    @Transactional
    @Override
    public CrmDtos.DealResponse convertLead(Long leadPrimeId, CrmDtos.ConvertDealRequest req) {
        CrmLeadEntity lead = leadRepository.findByLeadPrimeIdAndDeletedLeadFalse(leadPrimeId)
                .orElseThrow(() -> notFound("Lead not found: " + leadPrimeId));
        if (Boolean.TRUE.equals(lead.getLeadConverted()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Lead is already converted to a deal");
        if ("lost".equalsIgnoreCase(lead.getLeadOutcome()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A lost lead cannot be converted");
        if (!Boolean.TRUE.equals(lead.getContacted()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Mark the lead as contacted before converting it");

        CrmDealStage stage = (req.stage() == null || req.stage().isBlank()) ? CrmDealStage.QUALIFICATION : CrmDealStage.fromLabel(req.stage());
        if (!stage.isOpen())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial stage must be an open stage");

        CrmDealEntity d = new CrmDealEntity();
        d.setDealNumber("TMP-" + UUID.randomUUID());          // replaced right after we know the id
        d.setLeadPrimeId(lead.getLeadPrimeId());
        d.setLeadStrId(lead.getLeadStrId());
        d.setTitle(req.title().trim());
        d.setCompany(CrmActivityService.companyOrName(lead));
        d.setContact(((lead.getFirstName() == null ? "" : lead.getFirstName()) + " " + (lead.getLastName() == null ? "" : lead.getLastName())).trim());
        d.setAmount(req.amount() == null ? BigDecimal.ZERO : req.amount());
        d.setStage(stage);
        d.setExpectedClose(req.expectedClose());
        d = dealRepository.save(d);
        d.setDealNumber("DL-" + (1000 + d.getDealPrimeId()));

        lead.setLeadConverted(true);
        leadRepository.save(lead);

        activityService.log(lead.getLeadStrId(), d.getCompany(), "Deal", d.getDealNumber(), "Converted",
                "Lead converted to deal " + d.getDealNumber() + " (" + d.getTitle() + ")");
        return toResponse(d);
    }

    // ---------------- read ----------------
    @Transactional(readOnly = true)
    @Override
    public CrmDtos.PageResponse<CrmDtos.DealResponse> search(String view, String stage, String q, int page, int size) {
        String v = view == null ? "open" : view.toLowerCase();
        List<CrmDealStage> stages = switch (v) {
            case "won" -> List.of(CrmDealStage.CLOSED_WON);
            case "lost" -> List.of(CrmDealStage.CLOSED_LOST);
            default -> CrmDealStage.openStages();
        };
        CrmDealStage only = (stage == null || stage.isBlank() || "all".equalsIgnoreCase(stage)) ? null : CrmDealStage.fromLabel(stage);

        Specification<CrmDealEntity> spec = (r, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(r.get("stage").in(stages));
            if (only != null) p.add(cb.equal(r.get("stage"), only));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(r.get("dealNumber")), like),
                        cb.like(cb.lower(r.get("leadStrId")), like),
                        cb.like(cb.lower(r.get("title")), like),
                        cb.like(cb.lower(r.get("company")), like),
                        cb.like(cb.lower(r.get("contact")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        Sort sort = Sort.by(Sort.Direction.DESC, "won".equals(v) ? "closedAt" : "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "dealPrimeId"));
        Pageable pg = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);
        return CrmDtos.PageResponse.of(dealRepository.findAll(spec, pg).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    @Override
    public CrmDtos.DealResponse get(Long id) { return toResponse(find(id)); }

    @Transactional(readOnly = true)
    @Override
    public CrmDtos.DealStats stats() {
        List<CrmDealStage> open = CrmDealStage.openStages();
        BigDecimal pipeline = nz(dealRepository.sumAmountByStageIn(open));
        long won = dealRepository.countByStage(CrmDealStage.CLOSED_WON);
        BigDecimal wonValue = nz(dealRepository.sumAmountByStageIn(List.of(CrmDealStage.CLOSED_WON)));
        BigDecimal avg = won > 0 ? wonValue.divide(BigDecimal.valueOf(won), 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return new CrmDtos.DealStats(dealRepository.countByStageIn(open), pipeline, won, wonValue, avg,
                dealRepository.countByStage(CrmDealStage.CLOSED_LOST));
    }

    // ---------------- update ----------------
    @Transactional
    @Override
    public CrmDtos.DealResponse update(Long id, CrmDtos.DealUpdateRequest req) {
        CrmDealEntity d = requireOpen(find(id));
        d.setTitle(req.title().trim());
        d.setAmount(req.amount() == null ? BigDecimal.ZERO : req.amount());
        d.setExpectedClose(req.expectedClose());
        d.setNotes(req.notes() == null ? null : req.notes().trim());
        activityService.log(d.getLeadStrId(), d.getCompany(), "Deal", d.getDealNumber(), "Updated", "Deal details updated");
        return toResponse(d);
    }

    @Transactional
    @Override
    public CrmDtos.DealResponse changeStage(Long id, CrmDtos.StageRequest req) {
        CrmDealEntity d = requireOpen(find(id));
        CrmDealStage next = CrmDealStage.fromLabel(req.stage());
        if (!next.isOpen())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use the won / lost actions to close a deal");
        if (next != d.getStage()) {
            CrmDealStage old = d.getStage();
            d.setStage(next);
            activityService.log(d.getLeadStrId(), d.getCompany(), "Deal", d.getDealNumber(), "Stage Changed",
                    "Stage: " + old.getLabel() + " → " + next.getLabel());
        }
        return toResponse(d);
    }

    @Transactional
    @Override
    public CrmDtos.DealResponse markWon(Long id, CrmDtos.WonRequest req) {
        CrmDealEntity d = requireOpen(find(id));
        d.setAmount(req.amount());
        d.setStage(CrmDealStage.CLOSED_WON);
        d.setClosedAt(LocalDate.now());
        d.setLostReason(null);
        leadRepository.findById(d.getLeadPrimeId()).ifPresent(l -> {
            l.setLeadOutcome("won");           // feeds the existing won-reason report
            l.setLostReason(null);
        });
        activityService.log(d.getLeadStrId(), d.getCompany(), "Deal", d.getDealNumber(), "Closed Won",
                "Deal closed won — " + money(req.amount()));
        return toResponse(d);
    }

    @Transactional
    @Override
    public CrmDtos.DealResponse markLost(Long id, CrmDtos.LostRequest req) {
        CrmDealEntity d = requireOpen(find(id));
        d.setStage(CrmDealStage.CLOSED_LOST);
        d.setLostReason(req.reason().trim());
        d.setClosedAt(LocalDate.now());
        String txt = req.reason().trim() + (req.remarks() != null && !req.remarks().isBlank() ? " — " + req.remarks().trim() : "");
        leadRepository.findById(d.getLeadPrimeId()).ifPresent(l -> {
            l.setLeadOutcome("lost");          // feeds the existing lost-reason report
            l.setLostReason(txt);
        });
        activityService.log(d.getLeadStrId(), d.getCompany(), "Deal", d.getDealNumber(), "Closed Lost",
                "Deal closed lost: " + txt);
        return toResponse(d);
    }

    // ---------------- helpers ----------------
    private CrmDealEntity find(Long id) {
        return dealRepository.findById(id).orElseThrow(() -> notFound("Deal not found: " + id));
    }

    private CrmDealEntity requireOpen(CrmDealEntity d) {
        if (!d.getStage().isOpen())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deal " + d.getDealNumber() + " is already closed");
        return d;
    }

    private static ResponseStatusException notFound(String m) { return new ResponseStatusException(HttpStatus.NOT_FOUND, m); }

    private static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private static String money(BigDecimal a) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(0);
        return "₹" + nf.format(a);
    }

    private CrmDtos.DealResponse toResponse(CrmDealEntity d) {
        return new CrmDtos.DealResponse(d.getDealPrimeId(), d.getDealNumber(), d.getLeadPrimeId(), d.getLeadStrId(),
                d.getTitle(), d.getCompany(), d.getContact(), d.getAmount(),
                d.getStage().getLabel(), d.getStage().getProbability(),
                d.getExpectedClose(), d.getClosedAt(), d.getNotes(), d.getLostReason(), d.getCreatedAt());
    }
}