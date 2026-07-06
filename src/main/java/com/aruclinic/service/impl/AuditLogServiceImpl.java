package com.aruclinic.service.impl;

import com.aruclinic.entity.AuditLog;
import com.aruclinic.entity.User;
import com.aruclinic.repository.AuditLogRepository;
import com.aruclinic.repository.UserRepository;
import com.aruclinic.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AuditLog logAction(String action, Long entityId, String entityType, String userEmail, String details) {
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            // Try to find any user or default to system admin if not authenticated
            user = userRepository.findAll().stream().findFirst().orElse(null);
        }
        if (user == null) {
            return null;
        }
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityId(entityId)
                .entityType(entityType)
                .performedBy(user)
                .performedAt(LocalDateTime.now())
                .details(details)
                .build();
        return auditLogRepository.save(log);
    }

    @Override
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(int count) {
        return auditLogRepository.findTop10ByOrderByPerformedAtDesc();
    }
}
