package com.curatix.museumloan.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    /**
     * Records an audit event in its own transaction.
     * Failures in audit logging MUST NOT break the business transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, AuditAction action, String details) {
        try {
            ActorInfo actor = resolveActor();

            AuditLog entry = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .actorEmail(actor.email)
                    .actorRole(actor.role)
                    .ipAddress(actor.ip)
                    .details(details)
                    .createdAt(Instant.now())
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception ex) {
            // Never propagate — auditing must not break business logic
            log.warn("Failed to write audit log for {}#{} {}: {}", entityType, entityId, action, ex.getMessage());
        }
    }

    public void log(String entityType, Long entityId, AuditAction action) {
        log(entityType, entityId, action, null);
    }

    public Page<AuditLog> search(String entityType, String actorEmail, AuditAction action, Pageable pageable) {
        return auditLogRepository.search(entityType, actorEmail, action, pageable);
    }

    public List<AuditLog> historyOf(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    private ActorInfo resolveActor() {
        try {
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs instanceof ServletRequestAttributes servletAttrs) {
                HttpServletRequest req = servletAttrs.getRequest();
                String email = (String) req.getAttribute("email");
                String role = (String) req.getAttribute("role");
                String ip = extractClientIp(req);
                return new ActorInfo(
                        email != null ? email : "system",
                        role != null ? role : "SYSTEM",
                        ip
                );
            }
        } catch (Exception ignored) {}

        return new ActorInfo("system", "SYSTEM", null);
    }

    private String extractClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private record ActorInfo(String email, String role, String ip) {}
}