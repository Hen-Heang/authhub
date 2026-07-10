package com.henheang.securityapi.service;

import com.henheang.securityapi.domain.AuditEventType;

public interface AuditLogService {

    void log(AuditEventType eventType, Long userId, String identifier, String details);

    default void log(AuditEventType eventType, Long userId, String identifier) {
        log(eventType, userId, identifier, null);
    }
}
