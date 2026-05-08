package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("security_audit_log")
public class SecurityAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String eventType;
    private Long userId;
    private String role;
    private String path;
    private String detail;
    private String ip;
    private LocalDateTime createdAt;
}
