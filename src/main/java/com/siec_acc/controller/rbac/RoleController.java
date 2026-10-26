package com.siec_acc.controller.rbac;

import com.siec_acc.dto.rbac.ModuleInfoDto;
import com.siec_acc.dto.rbac.RoleDto;
import com.siec_acc.dto.rbac.RoleRequestDto;
import com.siec_acc.service.rbac.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) { this.roleService = roleService; }

    @GetMapping
    public List<RoleDto> list() { return roleService.list(); }

    @GetMapping("/modules")
    public List<ModuleInfoDto> modules() { return roleService.modules(); }

    @GetMapping("/{roleKey}")
    public RoleDto get(@PathVariable String roleKey) { return roleService.get(roleKey); }

    @PostMapping
    public ResponseEntity<RoleDto> create(@RequestBody RoleRequestDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.create(req));
    }

    @PutMapping("/{roleKey}")
    public RoleDto update(@PathVariable String roleKey, @RequestBody RoleRequestDto req) {
        return roleService.update(roleKey, req);
    }

    @DeleteMapping("/{roleKey}")
    public ResponseEntity<Void> delete(@PathVariable String roleKey) {
        roleService.delete(roleKey);
        return ResponseEntity.noContent().build();
    }

    // Body shape {"message": "..."} is what every page's apiFetch already reads
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handle(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason() == null ? "Request failed" : e.getReason()));
    }
}