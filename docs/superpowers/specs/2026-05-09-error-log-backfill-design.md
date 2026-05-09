# Error Log Backfill — Design Doc

**Created**: 2026-05-09
**Source**: Spec drift analysis of `003-business-logging`
**Scope**: 填补 FR-013 遗漏 + FR-006 黑名单审计缺口

## Background

`003-business-logging` spec 定义了 13 项功能需求，经 drift 分析发现：

- FR-013 未实现：缺少独立的 error.log 文件收集所有 WARN+ 级别日志
- FR-006 部分缺失：黑名单拦截事件无安全审计日志

本次仅修复这两项实际功能缺失，不涉及 spec 文本微调（FR-003/FR-008 的偏差为 spec 过度描述，功能正常运行）。

## Design

### 1. error.log (FR-013)

**新增 ERROR_FILE appender**，放在 logback-spring.xml 中 BUSINESS_FILE 之后：

```
appender: ERROR_FILE (RollingFileAppender)
  file: ./logs/error.log
  filter: ThresholdFilter WARN
  rollingPolicy: SizeAndTimeBasedRollingPolicy
    maxFileSize: 50MB
    maxHistory: 90          ← spec 要求（区别于其他日志的 30 天）
    totalSizeCap: 1GB
  encoder: 与现有 appender 一致的模式
```

**新增 ASYNC_ERROR**：异步包装，queueSize 512

**追加 appender-ref** 到以下位置：

| 位置 | 说明 |
|------|------|
| `<root>` (全局) | 非特定命名 logger 的 WARN/ERROR 汇入 |
| `springProfile default > root` | 开发环境 SYSTEM 日志的 WARN/ERROR |
| `springProfile prod > root` | 生产环境 SYSTEM 日志的 WARN/ERROR |

ACCESS/BUSINESS/SECURITY logger 的 WARN/ERROR 经 `additivity=false` 不从 root 输出，不考虑让其单独追加——这些是专项日志，运维排查时按需查看即可。error.log 覆盖的是 `com.agritainment.*` 命名空间的 WARN/ERROR（通过 root logger）。

### 2. 黑名单安全审计 (FR-006)

**位置**：`DiningService.java` 黑名单检测处

**改动**：抛出 AppException 之前，增加 SECURITY logger 记录 + `SecurityAuditLogService.logAsync()` 调用。

- 新增字段注入：`SecurityAuditLogService`
- 日志事件类型：`BLACKLIST_BLOCKED`
- 包含：userId、IP（从 HttpServletRequest 获取）、eventDetail（含 phone）

## Files Changed

| 文件 | 改动 |
|------|------|
| `server/src/main/resources/logback-spring.xml` | 新增 ERROR_FILE + ASYNC_ERROR，所有 root 追加 |
| `server/src/main/java/com/agritainment/service/DiningService.java` | 黑名单检测处增加安全审计日志 |

## Testing

- 现有单元测试应全部通过（不涉及已有测试文件变更）
- error.log: 启动应用后触发一个 404 请求，确认 error.log 中出现 WARN 级别日志
- 黑名单审计: 手动验证或通过代码审查确认