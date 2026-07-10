package com.henheang.securityapi.repository;

import com.henheang.securityapi.domain.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Page<AuditEvent> findAllByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
