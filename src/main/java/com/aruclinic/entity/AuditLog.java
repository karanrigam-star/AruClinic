package com.aruclinic.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * AuditLog entity to track changes performed by users.
 */
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action", nullable = false, length = 100)
    private String action; // e.g., CREATE_PATIENT, UPDATE_APPOINTMENT

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @CreationTimestamp
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    @Column(columnDefinition = "TEXT")
    private String details; // optional JSON or description of change

    public AuditLog() {
    }

    public AuditLog(Long id, String action, Long entityId, String entityType, User performedBy, LocalDateTime performedAt, String details) {
        this.id = id;
        this.action = action;
        this.entityId = entityId;
        this.entityType = entityType;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.details = details;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public User getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(User performedBy) {
        this.performedBy = performedBy;
    }

    public LocalDateTime getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private Long id;
        private String action;
        private Long entityId;
        private String entityType;
        private User performedBy;
        private LocalDateTime performedAt;
        private String details;

        public AuditLogBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder entityId(Long entityId) {
            this.entityId = entityId;
            return this;
        }

        public AuditLogBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public AuditLogBuilder performedBy(User performedBy) {
            this.performedBy = performedBy;
            return this;
        }

        public AuditLogBuilder performedAt(LocalDateTime performedAt) {
            this.performedAt = performedAt;
            return this;
        }

        public AuditLogBuilder details(String details) {
            this.details = details;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(id, action, entityId, entityType, performedBy, performedAt, details);
        }
    }
}