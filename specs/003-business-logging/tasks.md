# Tasks: Business Logging System

**Input**: Design documents from `/specs/003-business-logging/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, quickstart.md

**Reviews**: CEO Review (12 findings) + Eng Review (13 findings, 5 critical gaps) completed. All critical gaps incorporated below.

**Organization**: Tasks are grouped by phase to enable incremental delivery and testing.

## Format: `[ID] [P?] Description`

- **[P]**: Can run in parallel (different files, no dependencies)

## Path Conventions

- **Web app**: `server/src/main/java/com/agritainment/`, `server/src/main/resources/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 添加 AOP 依赖和创建 Logback 配置（含 AsyncAppender）

- [ ] T001 Add spring-boot-starter-aop dependency in `server/pom.xml`
- [ ] T002 Create `server/src/main/resources/logback-spring.xml` with:
  - Console appender (dev)
  - File appenders: access.log, business.log, security.log, system.log
  - **AsyncAppender** wrapping each file appender (Critical Gap #4 fix)
  - Rolling policy: 100MB per file, 30 days retention, 1GB total cap
  - JSON appender as optional Spring profile (taste decision)
- [ ] T003 Remove `logging.level` config from `server/src/main/resources/application.yml` (application-prod.yml does not exist — Eng finding #18)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 日志基础设施 — 脱敏工具类、MDC 上下文注入、@BusinessLog 注解

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Create `server/src/main/java/com/agritainment/common/SensitiveDataUtils.java` with:
  - Phone masking: `138****1234`
  - OpenID masking: `oXxYy****Zz`
  - Identity code masking: `110***********1234`
  - Password masking: `******`
  - **Precise code masking** (Critical Gap #3 fix): only mask `smsCode`/`verifyCode`, NOT `couponCode`/`qrCode`/`reservationCode`
  - Parameter truncation: max 500 chars (taste decision)
- [ ] T005 Create `server/src/main/java/com/agritainment/annotation/BusinessLog.java` — custom annotation for explicit business method marking (Critical Gap #2 fix: supplement prefix matching)
- [ ] T006 Create `server/src/main/java/com/agritainment/logging/BusinessLogAspect.java` with:
  - `@Around` advice on methods with @BusinessLog annotation OR service write method prefix matching
  - **@Order(Ordered.LOWEST_PRECEDENCE)** — ensure aspect runs AFTER transaction commit (Critical Gap #5 fix)
  - **Complete prefix coverage** (Critical Gap #2 fix): add login/register/adminLogin/changeTable/staffCancelReservation
  - Move `check*` prefix from skip list to write list for `checkNoShow` (Eng finding #16)
  - Param masking via SensitiveDataUtils
  - Result summary with truncation
- [ ] T007 Create `server/src/main/java/com/agritainment/interceptor/LoggingInterceptor.java` to inject MDC (requestId, userId, role) and clean up in afterCompletion
- [ ] T008 Create `server/src/main/java/com/agritainment/config/MdcTaskDecorator.java` — propagate MDC context to @Async threads (Critical Gap #1 fix: NotificationService uses @Async)
- [ ] T009 Register interceptors in `server/src/main/java/com/agritainment/config/WebConfig.java` with:
  - **Explicit order** (Eng finding #15): LoggingInterceptor(order=1) → AuthInterceptor(order=2) → RoleInterceptor(order=3)
  - LoggingInterceptor must run FIRST to set MDC before auth checks

**Checkpoint**: Foundation ready — MDC 上下文可用（含异步线程），脱敏工具可用，@BusinessLog 注解可用，Logback 异步配置生效

---

## Phase 3: User Story 1 - 运维人员排查线上问题 (Priority: P1) 🎯 MVP

**Goal**: 业务操作日志持久化到文件，运维人员可通过日志追溯用户操作历史和异常

**Independent Test**: 触发一个业务操作（如创建预约），检查日志文件中是否出现对应的业务日志和异常日志

### Implementation for User Story 1

- [ ] T010 [P] [US1] Create `server/src/main/java/com/agritainment/logging/RequestLoggingFilter.java` to log HTTP method, path, status code, duration with slow request marking (>3s)
- [ ] T011 [P] [US1] Enhance `server/src/main/java/com/agritainment/common/GlobalExceptionHandler.java` to:
  - Inject `HttpServletRequest` for request path and user context (Eng finding #9)
  - Log exception with requestId from MDC, request path, userId, and exception details

**Checkpoint**: 业务操作日志和异常日志写入文件，可通过 requestId 串联

---

## Phase 4: User Story 2 - 安全事件审计 (Priority: P2)

**Goal**: 认证失败、权限不足、黑名单拦截等安全事件被记录到日志和数据库

**Independent Test**: 使用无效 Token 访问受保护接口，检查日志中是否出现安全审计日志

### Implementation for User Story 2

- [ ] T012 [P] [US2] Enhance `server/src/main/java/com/agritainment/interceptor/AuthInterceptor.java` with:
  - **Distinguish token failure types** (Eng finding #8): TOKEN_MISSING, TOKEN_EXPIRED, TOKEN_INVALID, BLACKLISTED
  - Refactor catch block to parse specific exception types instead of generic Exception
  - Log security audit events with classification
- [ ] T013 [P] [US2] Add security audit logging (ROLE_DENIED) to `server/src/main/java/com/agritainment/interceptor/RoleInterceptor.java`
- [ ] T014 [US2] Create `security_audit_log` database table + entity + mapper for dual-write (taste decision: file + DB for non-repudiation)
  - Table: `security_audit_log` (id, event_type, user_id, role, path, detail, ip, created_at)
  - Entity: `server/src/main/java/com/agritainment/entity/SecurityAuditLog.java`
  - Mapper: `server/src/main/java/com/agritainment/mapper/SecurityAuditLogMapper.java`
  - Service: `server/src/main/java/com/agritainment/service/SecurityAuditLogService.java` with async DB write

**Checkpoint**: 安全事件 100% 被记录到日志文件和数据库

---

## Phase 5: User Story 3 - 请求访问监控 (Priority: P3)

**Goal**: 所有 HTTP 请求的访问日志被记录，包含耗时和状态码

**Independent Test**: 发送多个 HTTP 请求，检查日志文件中是否出现对应的访问日志

### Implementation for User Story 3

- [ ] T015 [US3] Register RequestLoggingFilter as Servlet Filter with /uploads exclusion in `server/src/main/java/com/agritainment/config/WebConfig.java`

**Checkpoint**: 所有 HTTP 请求的访问日志被记录，慢请求被标记

---

## Phase 6: Unit Tests (Critical — Eng Review Finding)

**Purpose**: 日志系统横切所有路径，必须有单元测试保障

- [ ] T016 [P] Unit test for SensitiveDataUtils — verify phone/openid/identityCode/password/smsCode masking, verify couponCode/qrCode NOT masked
- [ ] T017 [P] Unit test for BusinessLogAspect — verify @BusinessLog annotation triggers logging, verify @Order runs after transaction, verify prefix matching coverage
- [ ] T018 [P] Unit test for LoggingInterceptor — verify MDC injection and cleanup
- [ ] T019 [P] Unit test for MdcTaskDecorator — verify MDC context propagation to async threads
- [ ] T020 [P] Unit test for AuthInterceptor security audit — verify TOKEN_MISSING/TOKEN_EXPIRED/TOKEN_INVALID/BLACKLISTED classification

---

## Phase 7: Integration Validation

**Purpose**: 端到端验证

- [ ] T021 Start app, trigger business operations, verify log file output
- [ ] T022 Verify sensitive data masking in log output (phone, password, smsCode NOT couponCode)
- [ ] T023 Verify log rolling policy works (check file size limits)
- [ ] T024 Verify AsyncAppender non-blocking behavior under load
- [ ] T025 Verify security_audit_log database records match log file entries

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Phase 2 completion
  - US1 (Phase 3): Depends on Phase 2
  - US2 (Phase 4): Depends on Phase 2 (can parallel with US1)
  - US3 (Phase 5): Depends on US1 (RequestLoggingFilter created in T010)
- **Unit Tests (Phase 6)**: Can start after corresponding Phase 2-4 tasks
- **Integration (Phase 7)**: Depends on all user stories being complete

### Parallel Opportunities

- T001, T002, T003 can run in parallel (different files)
- T004, T005, T007, T008 can run in parallel (different files)
- T010, T011 can run in parallel (different files)
- T012, T013 can run in parallel (different files)
- T016, T017, T018, T019, T020 can run in parallel (different test files)

---

## Critical Gap Traceability

| Critical Gap | Fix Location | Task ID |
|-------------|-------------|---------|
| #1 @Async MDC 丢失 | MdcTaskDecorator | T008 |
| #2 AOP 前缀遗漏 | @BusinessLog + prefix supplement | T005, T006 |
| #3 `*code*` 脱敏误杀 | Precise matching in SensitiveDataUtils | T004 |
| #4 未配置 AsyncAppender | logback-spring.xml AsyncAppender | T002 |
| #5 AOP+事务代理顺序 | @Order on BusinessLogAspect | T006 |
| Eng #6 无测试任务 | Phase 6 unit tests | T016-T020 |
| Eng #8 Token 类型区分 | AuthInterceptor refactor | T012 |
| Eng #9 GlobalException 无上下文 | Inject HttpServletRequest | T011 |
| Eng #15 拦截器顺序 | WebConfig explicit order | T009 |
| Eng #16 checkNoShow | Move check* to write list | T006 |
| CEO #7 安全审计双写 | security_audit_log table | T014 |
| CEO #17 参数截断 | 500 char limit | T004, T006 |

---

## Notes

- [P] tasks = different files, no dependencies
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- All critical gaps from autoplan review are traced above
