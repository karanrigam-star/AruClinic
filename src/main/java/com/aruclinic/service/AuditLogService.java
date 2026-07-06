package com.aruclinic.service;

import com.aruclinic.entity.AuditLog;
import java.util.List;

public interface AuditLogService {
    AuditLog logAction(String action, Long entityId, String entityType, String userEmail, String details);
    List<AuditLog> getAllAuditLogs();
    List<AuditLog> getRecentAuditLogs(int count);
}
