package com.curatix.museumloan.audit;

import com.curatix.museumloan.config.SpringContext;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * JPA lifecycle listener that automatically writes audit entries
 * on entity persist / update / remove.
 *
 * Attach via @EntityListeners(AuditableEntityListener.class) on any entity.
 */
public class AuditableEntityListener {

    private static final Logger log = LoggerFactory.getLogger(AuditableEntityListener.class);

    @PostPersist
    public void onPostPersist(Object entity) {
        writeAudit(entity, AuditAction.CREATE);
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        writeAudit(entity, AuditAction.UPDATE);
    }

    @PostRemove
    public void onPostRemove(Object entity) {
        writeAudit(entity, AuditAction.DELETE);
    }

    private void writeAudit(Object entity, AuditAction action) {
        try {
            AuditService auditService = SpringContext.getBean(AuditService.class);
            String entityType = entity.getClass().getSimpleName();
            Long entityId = extractId(entity);
            auditService.log(entityType, entityId, action);
        } catch (Exception ex) {
            // Never fail the business transaction because of audit
            log.warn("Audit listener failed for {}: {}", entity.getClass().getSimpleName(), ex.getMessage());
        }
    }

    private Long extractId(Object entity) {
        try {
            Field idField = entity.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object value = idField.get(entity);
            return value instanceof Long l ? l : null;
        } catch (Exception ignored) {
            return null;
        }
    }
}