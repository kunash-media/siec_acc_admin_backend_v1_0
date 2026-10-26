package com.siec_acc.controller.rbac;

import com.siec_acc.dto.rbac.CreatorDto;
import com.siec_acc.entity.ActivityLogEntity;
import com.siec_acc.repository.ActivityLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@PreAuthorize("isAuthenticated()")
public class CreatedByController {

    private final ActivityLogRepository repository;

    public CreatedByController(ActivityLogRepository repository) { this.repository = repository; }

    // GET /api/activity/created-by?entityType=INVOICE&ids=12,13,14  ->  { "12": {adminId, adminName, createdAt}, ... }
    @GetMapping("/created-by")
    public Map<String, CreatorDto> createdBy(@RequestParam String entityType, @RequestParam List<String> ids) {
        if (ids.size() > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum 100 ids per request");

        Map<String, CreatorDto> out = new HashMap<>();
        repository.findByActionAndEntityTypeAndEntityIdIn("CREATE", entityType.trim().toUpperCase(), ids).stream()
                .sorted(Comparator.comparing(ActivityLogEntity::getId))
                .forEach(r -> out.putIfAbsent(r.getEntityId(),
                        new CreatorDto(r.getAdminId(), r.getAdminNameSnapshot(), r.getCreatedAt())));
        return out;
    }
}