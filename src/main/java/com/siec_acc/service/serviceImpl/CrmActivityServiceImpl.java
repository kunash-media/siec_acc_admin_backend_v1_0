package com.siec_acc.service.serviceImpl;


import com.siec_acc.dto.response.CrmDtos;
import com.siec_acc.entity.CrmActivityEntity;
import com.siec_acc.repository.CrmActivityRepository;
import com.siec_acc.service.CrmActivityService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrmActivityServiceImpl implements CrmActivityService {

    private final CrmActivityRepository repo;

    public CrmActivityServiceImpl(CrmActivityRepository repo) { this.repo = repo; }


    /** Joins the caller's transaction, so an activity is never written for a rolled-back change. */
    @Transactional
    @Override
    public void log(String leadNo, String company, String type, String ref, String action, String details) {
        CrmActivityEntity a = new CrmActivityEntity();
        a.setLeadStrId(leadNo);
        a.setCompany(company);
        a.setType(type);
        a.setRefNo(ref);
        a.setAction(action);
        a.setDetails(details != null && details.length() > 1000 ? details.substring(0, 1000) : details);
        repo.save(a);
    }

    @Transactional
    @Override
    public void deleteForLead(String leadNo) { repo.deleteByLeadStrId(leadNo); }

    @Transactional(readOnly = true)
    @Override
    public CrmDtos.PageResponse<CrmDtos.ActivityResponse> search(String type, String action, String q, int page, int size) {
        Specification<CrmActivityEntity> spec = (r, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (has(type)) p.add(cb.equal(r.get("type"), type));
            if (has(action)) p.add(cb.equal(r.get("action"), action));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(r.get("leadStrId")), like),
                        cb.like(cb.lower(r.get("company")), like),
                        cb.like(cb.lower(r.get("refNo")), like),
                        cb.like(cb.lower(r.get("details")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        Pageable pg = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
        return CrmDtos.PageResponse.of(repo.findAll(spec, pg).map(a -> new CrmDtos.ActivityResponse(
                a.getId(), a.getCreatedAt(), a.getLeadStrId(), a.getCompany(),
                a.getType(), a.getRefNo(), a.getAction(), a.getDetails())));
    }

    @Transactional(readOnly = true)
    @Override
    public CrmDtos.ActivityStats stats() {
        return new CrmDtos.ActivityStats(repo.count(),
                repo.countByCreatedAtGreaterThanEqual(LocalDate.now().atStartOfDay()),
                repo.countByAction("Converted"),
                repo.countByAction("Closed Won"));
    }

    private static boolean has(String s) { return s != null && !s.isBlank() && !"all".equalsIgnoreCase(s); }
}