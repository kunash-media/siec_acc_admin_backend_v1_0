package com.siec_acc.service.serviceImpl;

import com.siec_acc.service.CrmLeadService;
import com.siec_acc.service.CrmActivityService;
import com.siec_acc.dto.request.CrmLeadFollowupRequestDto;
import com.siec_acc.dto.request.CrmLeadRequestDto;
import com.siec_acc.dto.response.CrmLeadFollowupResponseDto;
import com.siec_acc.dto.response.CrmLeadResponseDto;
import com.siec_acc.dto.response.CrmMonthlyLeadCountDto;
import com.siec_acc.entity.CrmLeadEntity;
import com.siec_acc.entity.CrmLeadFollowupEntity;
import com.siec_acc.entity.CrmLeadRequirementCategoryEntity;
import com.siec_acc.entity.CrmStaffEntity;
import com.siec_acc.repository.CrmLeadFollowupRepository;
import com.siec_acc.repository.CrmLeadRepository;
import com.siec_acc.repository.CrmStaffRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrmLeadServiceImpl implements CrmLeadService {

    private static final List<String> TEMPS = List.of("hot", "warm", "cold");
    private static final List<String> PRIORITIES = List.of("P1", "P2", "P3");
    private static final String[] MONTHS = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    private final CrmLeadRepository leadRepository;
    private final CrmLeadFollowupRepository followupRepository;
    private final CrmStaffRepository staffRepository;
    private final CrmActivityService activityService;

    public CrmLeadServiceImpl(CrmLeadRepository leadRepository, CrmLeadFollowupRepository followupRepository,
                              CrmStaffRepository staffRepository, CrmActivityService activityService) {
        this.leadRepository = leadRepository;
        this.followupRepository = followupRepository;
        this.staffRepository = staffRepository;
        this.activityService = activityService;
    }

    // ------------------------------------------------------------------ create / update / delete

    @Transactional
    @Override
    public CrmLeadResponseDto createLead(CrmLeadRequestDto dto) {
        String phone = dto.phone().trim();
        String email = blankToNull(dto.email());
        if (leadRepository.existsByPhoneAndDeletedLeadFalse(phone))
            throw conflict("A lead with this phone number already exists");
        if (email != null && leadRepository.existsByEmailAndDeletedLeadFalse(email))
            throw conflict("A lead with this email already exists");

        CrmLeadEntity e = new CrmLeadEntity();
        apply(dto, e);
        e = leadRepository.save(e);
        e.setLeadStrId("LD-" + (1000 + e.getLeadPrimeId()));
        e = leadRepository.save(e);

        activityService.log(e.getLeadStrId(), CrmActivityService.companyOrName(e), "Lead", e.getLeadStrId(), "Registered",
                "Lead registered" + (e.getSource() != null ? " via " + e.getSource() : ""));
        return toResponse(e);
    }

    @Transactional
    @Override
    public CrmLeadResponseDto updateLead(Long id, CrmLeadRequestDto dto) {
        CrmLeadEntity e = find(id);
        if (Boolean.TRUE.equals(e.getLeadConverted()) || "lost".equalsIgnoreCase(e.getLeadOutcome()))
            throw conflict("Converted or lost leads cannot be edited");
        String phone = dto.phone().trim();
        String email = blankToNull(dto.email());
        if (leadRepository.existsByPhoneAndDeletedLeadFalseAndLeadPrimeIdNot(phone, id))
            throw conflict("A lead with this phone number already exists");
        if (email != null && leadRepository.existsByEmailAndDeletedLeadFalseAndLeadPrimeIdNot(email, id))
            throw conflict("A lead with this email already exists");

        apply(dto, e);
        e = leadRepository.save(e);
        activityService.log(e.getLeadStrId(), CrmActivityService.companyOrName(e), "Lead", e.getLeadStrId(), "Updated", "Lead details updated");
        return toResponse(e);
    }

    @Transactional(readOnly = true)
    @Override
    public CrmLeadResponseDto getLead(Long id) { return toResponse(find(id)); }

    @Transactional
    @Override
    public void deleteLead(Long id) {
        CrmLeadEntity e = find(id);
        if (Boolean.TRUE.equals(e.getLeadConverted()))
            throw conflict("This lead has a deal and cannot be deleted");
        e.setDeletedLead(true);                      // soft delete
        leadRepository.save(e);
        activityService.deleteForLead(e.getLeadStrId());
    }

    @Transactional(readOnly = true)
    @Override
    public CrmLeadResponseDto checkPhoneExists(String phone) {
        if (phone == null || phone.trim().length() < 10) return null;
        return leadRepository.findByPhoneAndDeletedLeadFalse(phone.trim()).map(this::toResponse).orElse(null);
    }

    // ------------------------------------------------------------------ pipeline views (CRM page)

    /** stage = registered | contacted | converted | lost | all */
    @Transactional(readOnly = true)
    @Override
    public Page<CrmLeadResponseDto> searchPipeline(String stage, String source, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "leadPrimeId")));
        return leadRepository.findAll(pipelineSpec(stage, source, q), pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Long> pipelineCounts() {
        LocalDate today = LocalDate.now();
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("total", leadRepository.count(pipelineSpec("all", null, null)));
        for (String s : List.of("registered", "contacted", "converted", "lost"))
            m.put(s, leadRepository.count(pipelineSpec(s, null, null)));
        Specification<CrmLeadEntity> overdue = (r, cq, cb) -> cb.and(
                cb.isNotNull(r.get("followUpDate")),
                cb.lessThan(r.<LocalDate>get("followUpDate"), today),
                cb.or(cb.isNull(r.get("followupStatus")), cb.notEqual(r.get("followupStatus"), "done")));
        m.put("overdue", leadRepository.count(pipelineSpec("contacted", null, null).and(overdue)));
        return m;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrmLeadResponseDto> getPendingFollowups() {
        return leadRepository.findPendingFollowups().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public CrmLeadResponseDto markContacted(Long id, String note, LocalDate nextFollowup) {
        CrmLeadEntity lead = find(id);
        if (Boolean.TRUE.equals(lead.getLeadConverted()) || "lost".equalsIgnoreCase(lead.getLeadOutcome()))
            throw conflict("Only open leads can be marked as contacted");
        lead.setContacted(true);
        lead.setFollowUpDate(nextFollowup);
        lead.setFollowupStatus(nextFollowup != null ? "pending" : null);
        leadRepository.save(lead);
        activityService.log(lead.getLeadStrId(), CrmActivityService.companyOrName(lead), "Lead", lead.getLeadStrId(),
                "Contacted", "Marked as contacted: " + note);
        return toResponse(lead);
    }

    // ------------------------------------------------------------------ follow-ups

    @Transactional
    @Override
    public CrmLeadResponseDto addFollowup(Long id, CrmLeadFollowupRequestDto dto) {
        CrmLeadEntity lead = find(id);
        if (Boolean.TRUE.equals(lead.getLeadConverted()) || "lost".equalsIgnoreCase(lead.getLeadOutcome()))
            throw conflict("Follow-ups can only be added to open leads");
        String status = dto.followupStatus().trim().toLowerCase();
        if (!status.equals("done") && !status.equals("pending")) throw bad("Follow-up status must be 'done' or 'pending'");

        CrmLeadFollowupEntity f = new CrmLeadFollowupEntity();
        f.setLead(lead);
        f.setFollowupDate(dto.followupDate());
        f.setFollowupStatus(status);
        f.setFollowupNotes(blankToNull(dto.followupNotes()));
        f.setMeetingType(blankToNull(dto.meetingType()));
        if (dto.takenByStaffId() != null) f.setTakenBy(staff(dto.takenByStaffId()));
        followupRepository.save(f);

        // the newest entry decides the lead's current follow-up snapshot
        if (status.equals("done")) {
            lead.setFollowupStatus("done");
        } else {
            lead.setFollowUpDate(dto.followupDate());
            lead.setFollowupStatus("pending");
        }
        leadRepository.save(lead);

        StringBuilder txt = new StringBuilder(status.equals("done") ? "Follow-up done" : "Next follow-up scheduled for " + dto.followupDate());
        List<String> meta = new ArrayList<>();
        if (f.getMeetingType() != null) meta.add(f.getMeetingType());
        if (f.getTakenBy() != null) meta.add("by " + f.getTakenBy().getStaffFirstName());
        if (!meta.isEmpty()) txt.append(" (").append(String.join(", ", meta)).append(")");
        if (f.getFollowupNotes() != null) txt.append(": ").append(f.getFollowupNotes());
        activityService.log(lead.getLeadStrId(), CrmActivityService.companyOrName(lead), "Lead", lead.getLeadStrId(), "Follow-up", txt.toString());
        return toResponse(lead);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrmLeadFollowupResponseDto> getFollowupHistory(Long id) {
        find(id);
        return followupRepository.findByLead_LeadPrimeIdAndDeletedFollowupFalseOrderByFollowupDateDescFollowupPrimeIdDesc(id).stream()
                .map(f -> new CrmLeadFollowupResponseDto(f.getFollowupPrimeId(), f.getFollowupDate(), f.getFollowupStatus(),
                        f.getFollowupNotes(), f.getMeetingType(),
                        f.getTakenBy() == null ? null : f.getTakenBy().getStaffPrimeId(),
                        f.getTakenBy() == null ? null : fullName(f.getTakenBy()), f.getCreatedAt()))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ outcome + stats

    /** outcome = won | lost | open ("open" re-opens a lost lead). */
    @Transactional
    @Override
    public void updateLeadOutcome(Long id, String outcome, String lostReason) {
        CrmLeadEntity lead = find(id);
        String o = outcome.trim().toLowerCase();
        if (!List.of("won", "lost", "open").contains(o)) throw bad("leadOutcome must be won, lost or open");
        if (o.equals("lost") && blankToNull(lostReason) == null) throw bad("A reason is required when marking a lead as lost");
        if (o.equals("lost") && Boolean.TRUE.equals(lead.getLeadConverted()))
            throw conflict("This lead is already a deal. Close the deal as lost instead.");

        lead.setLeadOutcome(o.equals("open") ? null : o);
        lead.setLostReason(o.equals("lost") ? lostReason.trim() : null);
        leadRepository.save(lead);
        if (o.equals("lost"))
            activityService.log(lead.getLeadStrId(), CrmActivityService.companyOrName(lead), "Lead", lead.getLeadStrId(),
                    "Lead Lost", "Marked lost: " + lead.getLostReason());
    }

    @Transactional(readOnly = true)
    @Override
    public List<CrmMonthlyLeadCountDto> getMonthlyLeadCounts(int monthsBack) {
        int n = Math.min(Math.max(monthsBack, 1), 24);
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.minusMonths(n - 1L).withDayOfMonth(1).atStartOfDay();
        Map<String, Long> counts = leadRepository.findByDeletedLeadFalseAndCreatedAtGreaterThanEqual(from).stream()
                .collect(Collectors.groupingBy(l -> YearMonth.from(l.getCreatedAt()).toString(), Collectors.counting()));
        List<CrmMonthlyLeadCountDto> out = new ArrayList<>();
        for (int i = n - 1; i >= 0; i--) {
            YearMonth ym = YearMonth.from(today.minusMonths(i));
            out.add(new CrmMonthlyLeadCountDto(ym.toString(), MONTHS[ym.getMonthValue() - 1], ym.getYear(), counts.getOrDefault(ym.toString(), 0L)));
        }
        return out;
    }

    // ------------------------------------------------------------------ helpers

    private CrmLeadEntity find(Long id) {
        return leadRepository.findByLeadPrimeIdAndDeletedLeadFalse(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found: " + id));
    }

    private CrmStaffEntity staff(Long id) {
        return staffRepository.findById(id).orElseThrow(() -> bad("Staff not found: " + id));
    }

    private void apply(CrmLeadRequestDto d, CrmLeadEntity e) {
        e.setFirstName(d.firstName().trim());
        e.setLastName(d.lastName() == null ? "" : d.lastName().trim());
        e.setEmail(blankToNull(d.email()));
        e.setPhone(d.phone().trim());
        e.setCompany(blankToNull(d.company()));
        e.setDesignation(blankToNull(d.designation()));
        e.setCity(blankToNull(d.city()));

        String temp = blankToNull(d.status());
        temp = temp == null ? "warm" : temp.toLowerCase();
        if (!TEMPS.contains(temp)) throw bad("Status must be hot, warm or cold");
        e.setStatus(temp);

        String prio = blankToNull(d.priority());
        prio = prio == null ? "P2" : prio.toUpperCase();
        if (!PRIORITIES.contains(prio)) throw bad("Priority must be P1, P2 or P3");
        e.setPriority(prio);

        e.setSource(blankToNull(d.source()));
        e.setReferralDetails("Referral".equalsIgnoreCase(e.getSource()) ? blankToNull(d.referralDetails()) : null);
        e.setTags(blankToNull(d.tags()));
        e.setNotes(blankToNull(d.notes()));
        e.setFollowUpDate(d.followUpDate());
        e.setFollowupStatus(d.followUpDate() == null ? null : (blankToNull(d.followupStatus()) == null ? "pending" : d.followupStatus().trim().toLowerCase()));

        e.getRequirementCategories().clear();
        if (d.requirementCategory() != null) {
            new LinkedHashSet<>(d.requirementCategory()).stream().map(CrmLeadServiceImpl::blankToNull).filter(Objects::nonNull)
                    .forEach(c -> e.getRequirementCategories().add(new CrmLeadRequirementCategoryEntity(e, c)));
        }
        e.setAssignedStaff(d.assignedStaffId() == null ? null : staff(d.assignedStaffId()));
    }

    private Specification<CrmLeadEntity> pipelineSpec(String stage, String source, String q) {
        return (r, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.isFalse(r.get("deletedLead")));

            Predicate converted = cb.isTrue(r.get("leadConverted"));
            Predicate notConverted = cb.or(cb.isNull(r.get("leadConverted")), cb.isFalse(r.get("leadConverted")));
            Predicate lost = cb.equal(cb.lower(r.get("leadOutcome")), "lost");
            Predicate notLost = cb.or(cb.isNull(r.get("leadOutcome")), cb.notEqual(cb.lower(r.get("leadOutcome")), "lost"));
            Predicate contacted = cb.isTrue(r.get("contacted"));
            Predicate notContacted = cb.or(cb.isNull(r.get("contacted")), cb.isFalse(r.get("contacted")));

            switch (stage == null ? "all" : stage.toLowerCase()) {
                case "converted" -> p.add(converted);
                case "lost" -> { p.add(notConverted); p.add(lost); }
                case "contacted" -> { p.add(notConverted); p.add(notLost); p.add(contacted); }
                case "registered" -> { p.add(notConverted); p.add(notLost); p.add(notContacted); }
                default -> { }
            }
            if (source != null && !source.isBlank() && !"all".equalsIgnoreCase(source)) p.add(cb.equal(r.get("source"), source));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(cb.like(cb.lower(r.get("leadStrId")), like), cb.like(cb.lower(r.get("firstName")), like),
                        cb.like(cb.lower(cb.coalesce(r.<String>get("lastName"), "")), like),
                        cb.like(cb.lower(cb.coalesce(r.<String>get("company"), "")), like),
                        cb.like(cb.lower(r.get("phone")), like),
                        cb.like(cb.lower(cb.coalesce(r.<String>get("email"), "")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }

    private CrmLeadResponseDto toResponse(CrmLeadEntity e) {
        CrmStaffEntity s = e.getAssignedStaff();
        return new CrmLeadResponseDto(e.getLeadPrimeId(), e.getLeadStrId(), e.getFirstName(), e.getLastName(), e.getEmail(),
                e.getPhone(), e.getCompany(), e.getDesignation(), e.getCity(), e.getStatus(), e.getPriority(), e.getSource(),
                e.getReferralDetails(),
                e.getRequirementCategories().stream().map(CrmLeadRequirementCategoryEntity::getCategory).collect(Collectors.toList()),
                e.getTags(), e.getFollowUpDate(), e.getFollowupStatus(), e.getNotes(),
                e.getLeadConverted(), e.getContacted(), e.getLeadOutcome(), e.getLostReason(), e.getPipelineStatus(),
                s == null ? null : s.getStaffPrimeId(), s == null ? null : fullName(s), e.getCreatedAt(), e.getUpdatedAt());
    }

    private static String fullName(CrmStaffEntity s) {
        return ((s.getStaffFirstName() == null ? "" : s.getStaffFirstName()) + " " + (s.getStaffLastName() == null ? "" : s.getStaffLastName())).trim();
    }
    private static String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }
    private static ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
    private static ResponseStatusException conflict(String m) { return new ResponseStatusException(HttpStatus.CONFLICT, m); }
}