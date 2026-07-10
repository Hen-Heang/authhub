package com.henheang.securityapi.controller;

import com.henheang.securityapi.domain.AuditEvent;
import com.henheang.securityapi.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AuditController extends BaseController {

    private final AuditEventRepository auditEventRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Object list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ok(auditEventRepository.findAll(pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public Object listForUser(@PathVariable Long userId,
                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ok(auditEventRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable));
    }
}
