package com.agritainment.service;

import com.agritainment.entity.SecurityAuditLog;
import com.agritainment.mapper.SecurityAuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditLogService {

    private final SecurityAuditLogMapper auditLogMapper;

    @Async
    public void logAsync(String eventType, Long userId, String role, String path, String detail, String ip) {
        SecurityAuditLog auditLog = new SecurityAuditLog();
        auditLog.setEventType(eventType);
        auditLog.setUserId(userId);
        auditLog.setRole(role);
        auditLog.setPath(path);
        auditLog.setDetail(detail);
        auditLog.setIp(ip);
        auditLog.setCreatedAt(java.time.LocalDateTime.now());
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.error("Failed to persist security audit log: {}", e.getMessage());
        }
    }
}
