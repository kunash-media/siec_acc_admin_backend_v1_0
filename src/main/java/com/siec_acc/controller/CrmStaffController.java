package com.siec_acc.controller;

import com.siec_acc.dto.request.CrmStaffRequestDto;
import com.siec_acc.dto.response.CrmStaffResponseDto;
import com.siec_acc.entity.CrmStaffEntity;
import com.siec_acc.repository.CrmStaffRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Minimal staff API (dropdown for "Assign to" / "Taken by"). Swap for your ERP's staff module when it exists. */
@RestController
@RequestMapping("/api/crm/v1/staff")
public class CrmStaffController {

    private final CrmStaffRepository repo;

    public CrmStaffController(CrmStaffRepository repo) { this.repo = repo; }

    @GetMapping("/dropdown")
    public List<CrmStaffResponseDto> dropdown() {
        return repo.findByActiveTrueOrderByStaffFirstNameAsc().stream()
                .map(s -> new CrmStaffResponseDto(s.getStaffPrimeId(), s.getStaffFirstName(), s.getStaffLastName(), s.getStaffRole()))
                .toList();
    }

    @PostMapping
    public ResponseEntity<CrmStaffResponseDto> create(@RequestBody @Valid CrmStaffRequestDto d) {
        CrmStaffEntity s = new CrmStaffEntity();
        s.setStaffFirstName(d.staffFirstName().trim());
        s.setStaffLastName(d.staffLastName());
        s.setStaffRole(d.staffRole());
        s.setStaffEmail(d.staffEmail());
        s.setStaffMobile(d.staffMobile());
        s = repo.save(s);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CrmStaffResponseDto(s.getStaffPrimeId(), s.getStaffFirstName(), s.getStaffLastName(), s.getStaffRole()));
    }

    /** Soft-remove: the person disappears from dropdowns but old follow-ups keep their name. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        repo.findById(id).ifPresent(s -> { s.setActive(false); repo.save(s); });
        return ResponseEntity.noContent().build();
    }
}