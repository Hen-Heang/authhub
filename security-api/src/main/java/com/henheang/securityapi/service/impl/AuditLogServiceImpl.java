package com.henheang.securityapi.service.impl;

import com.henheang.securityapi.domain.AuditEvent;
import com.henheang.securityapi.domain.AuditEventType;
import com.henheang.securityapi.repository.AuditEventRepository;
import com.henheang.securityapi.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditEventRepository auditEventRepository;

    @Override
    // A failed audit write must never roll back or block the auth flow it's
    // observing, so it runs in its own transaction and swallows errors.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditEventType eventType, Long userId, String identifier, String details) {
        try {
            AuditEvent event = new AuditEvent();
            event.setEventType(eventType);
            event.setUserId(userId);
            event.setIdentifier(identifier);
            event.setDetails(details);
            event.setIpAddress(currentClientIp());
            event.setUserAgent(currentUserAgent());
            auditEventRepository.save(event);
        } catch (Exception e) {
            logger.error("Failed to write audit log for event {}: {}", eventType, e.getMessage(), e);
        }
    }

    private String currentClientIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String currentUserAgent() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : request.getHeader("User-Agent");
    }

    private HttpServletRequest currentRequest() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        return attrs.getRequest();
    }
}
