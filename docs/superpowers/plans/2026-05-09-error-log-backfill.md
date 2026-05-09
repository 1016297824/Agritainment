# Error Log Backfill — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 填补 `003-business-logging` spec 的 FR-013（error.log）和 FR-006（黑名单安全审计）

**Architecture:** 在 logback-spring.xml 新增 ERROR_FILE appender + ASYNC_ERROR 异步包装，所有 root logger 追加引用。在 DiningService 黑名单检测处复用 AuthInterceptor 的安全审计双写模式（SECURITY logger + SecurityAuditLogService.logAsync()）

**Tech Stack:** Spring Boot 3.2.5, Logback, MyBatis-Plus, SLF4J

---

### Task 1: logback-spring.xml — 新增 ERROR_FILE appender

**Files:**
- Modify: `server/src/main/resources/logback-spring.xml`

- [ ] **Step 1: 在 SYSTEM_FILE appender 之后插入 ERROR_FILE appender**

在 `</appender>` （SYSTEM_FILE 结束标签，第62行）之后插入：

```xml
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/error.log</file>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>WARN</level>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/error.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>90</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>
```

- [ ] **Step 2: 在 ASYNC_SYSTEM appender 之后插入 ASYNC_ERROR appender**

在 `</appender>`（ASYNC_SYSTEM 结束标签，第90行）之后插入：

```xml
    <appender name="ASYNC_ERROR" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <neverBlock>false</neverBlock>
        <appender-ref ref="ERROR_FILE"/>
    </appender>
```

- [ ] **Step 3: 全局 `<root>` 追加 ASYNC_ERROR**

修改第92行前（在 `<logger name="ACCESS"` 之前），在 `</appender>` 之后追加一个全局 root：

但注意当前文件没有显式的全局 `<root>`。阅读文件发现全局配置通过 `<springProfile name="default">` 和 `<springProfile name="prod">` 中的 `<root>` 覆盖。

因此改为：在 default profile 的 root 和 prod profile 的 root 中各追加 `<appender-ref ref="ASYNC_ERROR"/>`。

- [ ] **Step 4: default profile root 追加 ASYNC_ERROR**

修改第105-108行：

```xml
    <springProfile name="default">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="ASYNC_SYSTEM"/>
            <appender-ref ref="ASYNC_ERROR"/>
        </root>
```

- [ ] **Step 5: prod profile root 追加 ASYNC_ERROR**

修改第125-127行：

```xml
    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="ASYNC_SYSTEM"/>
            <appender-ref ref="ASYNC_ERROR"/>
        </root>
```

- [ ] **Step 6: 编译验证**

```bash
cd server; mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 7: Commit**

```bash
git add server/src/main/resources/logback-spring.xml
git commit -m "feat(logging): add error.log appender with 90-day retention (FR-013)"
```

---

### Task 2: DiningService — 黑名单安全审计

**Files:**
- Modify: `server/src/main/java/com/agritainment/service/DiningService.java`

- [ ] **Step 1: 新增依赖注入**

在第32行（`private final NotificationService notificationService;`）之后，追加：

```java
    private final SecurityAuditLogService securityAuditLogService;
    private final HttpServletRequest request;
```

SecurityAuditLogService 会随 `@RequiredArgsConstructor` 自动注入。HttpServletRequest 通过 Spring 的 request-scoped proxy 自动注入。

- [ ] **Step 2: 新增 import**

在文件顶部 import 区域追加：

```java
import com.agritainment.service.SecurityAuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
```

- [ ] **Step 3: 新增 SECURITY logger 常量**

在 `@RequiredArgsConstructor` 注解后、第一个 field 之前，追加：

```java
    private static final Logger secLog = LoggerFactory.getLogger("SECURITY");
```

- [ ] **Step 4: 黑名单检测处增加安全审计日志**

将第62行的：

```java
        if (Boolean.TRUE.equals(dbUser.getIsBlacklisted())) throw new AppException(40001, "您已被加入黑名单，无法预约");
```

替换为：

```java
        if (Boolean.TRUE.equals(dbUser.getIsBlacklisted())) {
            secLog.warn("[BLACKLIST_BLOCKED] userId={} phone={} path=/api/v1/dining/reservations detail=黑名单用户尝试预约",
                    userId, dbUser.getPhone());
            securityAuditLogService.logAsync("BLACKLIST_BLOCKED", userId, null,
                    "/api/v1/dining/reservations", "黑名单用户尝试预约，phone=" + dbUser.getPhone(),
                    com.agritainment.common.IpUtils.getClientIp(request));
            throw new AppException(40001, "您已被加入黑名单，无法预约");
        }
```

- [ ] **Step 5: 编译验证**

```bash
cd server; mvn compile -q
```
Expected: BUILD SUCCESS

- [ ] **Step 6: 运行单元测试**

```bash
cd server; mvn test -q
```
Expected: BUILD SUCCESS, 所有已有测试通过

- [ ] **Step 7: Commit**

```bash
git add server/src/main/java/com/agritainment/service/DiningService.java
git commit -m "feat(logging): add security audit log for blacklist blocked events (FR-006)"
```

---

## GSTACK REVIEW REPORT

| Review | Trigger | Why | Runs | Status | Findings |
|--------|---------|-----|------|--------|----------|
| CEO Review | `/autoplan` | Scope & strategy | 1 | CLEAR | 0 proposals, micro-change |
| Eng Review | `/plan-eng-review` | Architecture & tests (required) | 1 | CLEAR | 1 issue (fixed), 0 critical gaps |
| Design Review | — | No UI/UX scope | 0 | — | — |
| DX Review | — | No DX scope | 0 | — | — |

- **UNRESOLVED:** 0
- **VERDICT:** CEO + ENG CLEARED — ready to implement