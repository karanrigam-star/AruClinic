package com.aruclinic.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for AuditLog entity.
 */
public class AuditLogDto {

    private Long id;
    private String action;
    private String entityId;
    private String entityType;
    private String performedBy;
    private LocalDateTime performedAt;
    private String details;

    public AuditLogDto() {}

    public AuditLogDto(Long id, String action, String entityId, String entityType,
                       String performedBy, LocalDateTime performedAt, String details) {
        this.id = id;
        this.action = action;
        this.entityId = entityId;
        this.entityType = entityType;
        this.performedBy = performedBy;
        this.performedAt = performedAt;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}