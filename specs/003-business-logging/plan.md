# Implementation Plan: Business Logging System

**Branch**: `003-business-logging` | **Date**: 2026-05-08 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/003-business-logging/spec.md`

## Summary

为农家乐小程序后端增加全面的业务日志系统，采用 AOP 切面 + HandlerInterceptor + Servlet Filter 方案，实现四层日志架构（访问日志、业务日志、安全审计日志、系统日志），日志持久化到本地文件并配置滚动策略，支持请求 ID 串联和敏感数据脱敏。

## Technical Context

**Language/Version**: Java 21
**Primary Dependencies**: Spring Boot 3.2.5, SLF4J + Logback (自带), Spring AOP (spring-boot-starter-web 自带), Lombok
**Storage**: 本地文件系统 (./logs/ 目录)
**Testing**: spring-boot-starter-test
**Target Platform**: Linux 服务器 (单机部署)
**Project Type**: web-service (Spring Boot REST API)
**Performance Goals**: 日志写入不影响业务请求响应时间（异步写入，耗时 < 1ms）
**Constraints**: 不引入新依赖，使用 Spring Boot 自带组件
**Scale/Scope**: 9 Controller + 12 Service + 2 Interceptor，约 5 个新文件 + 6 个修改文件

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 原则 | 检查结果 | 说明 |
|------|---------|------|
| I. 用户体验优先 | ✅ 通过 | 日志系统对用户透明，不影响用户体验 |
| II. 安全底线 | ✅ 通过 | 敏感数据脱敏（手机号、openid、密码、验证码），安全事件审计日志 |
| III. 一致性 | ✅ 通过 | 使用项目已有的 SLF4J + Logback，遵循 Spring Boot 标准模式 |
| IV. 渐进增强 | ✅ 通过 | 分层实现，先基础（文件输出+滚动），再业务（AOP切面），最后安全审计 |

**Technology Constraints 检查**:

| 约束 | 检查结果 |
|------|---------|
| 后端框架 Spring Boot 3.2 + Java 21 + MyBatis-Plus | ✅ 不引入新框架 |
| 不引入其他前端框架 | ✅ 仅后端改动 |
| JWT 认证 | ✅ 拦截器日志复用现有 JWT 机制 |

**Quality Gates 检查**:

| 关卡 | 计划 |
|------|------|
| 功能验证 | 启动应用，触发业务操作，检查日志文件输出 |
| 回归检查 | 现有功能不受影响，AOP 切面仅记录日志不修改业务逻辑 |
| 类型检查 | Maven 编译无错误 |
| 代码审查 | 通过 /review 流程 |
| 完整性 | 覆盖所有写操作、安全事件、异常场景 |

## Project Structure

### Documentation (this feature)

```text
specs/003-business-logging/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── spec.md              # Feature specification
├── checklists/          # Quality checklists
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code (repository root)

```text
server/src/main/java/com/agritainment/
├── common/
│   ├── Result.java                          # (existing, no change)
│   ├── AppException.java                    # (existing, no change)
│   ├── GlobalExceptionHandler.java          # (MODIFY: 增强异常日志)
│   └── SensitiveDataUtils.java              # (NEW: 脱敏工具类)
├── config/
│   ├── WebConfig.java                       # (MODIFY: 注册 LoggingInterceptor)
│   ├── MybatisPlusConfig.java               # (existing, no change)
│   └── IndexInitializer.java                # (existing, no change)
├── interceptor/
│   ├── AuthInterceptor.java                 # (MODIFY: 增加安全审计日志)
│   ├── RoleInterceptor.java                 # (MODIFY: 增加安全审计日志)
│   └── LoggingInterceptor.java              # (NEW: MDC 注入 + 请求上下文)
├── logging/
│   ├── RequestLoggingFilter.java            # (NEW: 请求访问日志)
│   └── BusinessLogAspect.java               # (NEW: 业务操作日志 AOP 切面)
├── controller/                              # (9 files, no change)
├── service/                                 # (12 files, no change)
└── ...

server/src/main/resources/
├── logback-spring.xml                       # (NEW: Logback 配置)
├── application.yml                          # (MODIFY: 移除 logging.level)
└── application-prod.yml                     # (MODIFY: 移除 logging.level)
```

**Structure Decision**: 在现有项目结构中新增 `logging/` 包存放日志相关组件，新增 `SensitiveDataUtils.java` 放在 `common/` 包（与现有工具类一致），`LoggingInterceptor.java` 放在 `interceptor/` 包（与现有拦截器一致）。

## Complexity Tracking

无 Constitution 违规需要记录。

<!-- AUTONOMOUS DECISION LOG -->
## Decision Audit Trail

| # | Phase | Decision | Classification | Principle | Rationale | Rejected |
|---|-------|----------|-----------|-----------|----------|----------|
| 1 | CEO | 问题框定：保留"日志系统"，增加可观测性扩展路径说明 | Taste | P6 行动优先 | 当前范围足够，扩展路径留文档 | 扩展为可观测性体系 |
| 2 | CEO | AOP 前缀遗漏：补全前缀 + 添加 @BusinessLog 注解 | Mechanical | P1+P5 | login/register 遗漏是安全盲区 | 仅补全前缀 |
| 3 | CEO | `*code*` 脱敏：改为精确匹配 smsCode/verifyCode | Mechanical | P1 完整性 | 过度匹配误杀 couponCode/qrCode | 保留通配符 |
| 4 | CEO | 单机假设：保留，添加 hostname 字段和 JSON 预留 | Taste | P1+P6 | 当前单机足够，预留迁移路径 | 立即迁移 JSON |
| 5 | CEO | AsyncAppender：在 logback-spring.xml 中添加 | Mechanical | P1 完整性 | 声称异步但未配置，必须修复 | 保持同步 |
| 6 | CEO | MDC 异步线程：添加 MdcTaskDecorator | Mechanical | P1 完整性 | @Async 线程丢失 MDC 是已知问题 | 忽略异步场景 |
| 7 | CEO | 安全审计双写：添加 security_audit_log 数据库表 | Taste | P1+P2 沸湖 | 文件可被篡改，审计需要不可篡改存储 | 仅存文件 |
| 8 | CEO | 日志文件访问控制：在 plan 中注明权限要求 | Mechanical | P1 完整性 | 安全最佳实践 | 不提及 |
| 9 | CEO | 注解方案：采用 @BusinessLog + 前缀匹配混合 | Taste | P5 显式优于巧妙 | 注解显式，前缀匹配作为 fallback | 仅用前缀匹配 |
| 10 | CEO | JSON 日志：保留纯文本，添加 JSON Appender 可选 | Taste | P6+P1 | 当前纯文本够用，预留 JSON | 全部改 JSON |
| 11 | Eng | @Async MDC 丢失：实现 MdcTaskDecorator | Mechanical | P1 完整性 | NotificationService 使用 @Async | 忽略 |
| 12 | Eng | AOP+事务顺序：BusinessLogAspect 添加 @Order | Mechanical | P1 完整性 | 事务回滚时日志显示成功是数据不一致 | 不处理 |
| 13 | Eng | 无测试任务：添加核心组件单元测试和集成测试 | Mechanical | P1 完整性 | 日志系统横切所有路径，必须测试 | 跳过测试 |
| 14 | Eng | Token 类型区分：重构 AuthInterceptor 异常处理 | Mechanical | P1 完整性 | 安全审计需要区分失败类型 | 统一处理 |
| 15 | Eng | 拦截器顺序：显式指定 order，拆分 MDC 职责 | Mechanical | P1 完整性 | 顺序错误导致 MDC 缺失 | 依赖注册顺序 |
| 16 | Eng | check 前缀冲突：将 check* 移到写操作列表 | Mechanical | P1 完整性 | checkNoShow 是写操作被跳过 | 添加特殊处理 |
| 17 | Eng | 参数截断：添加最大长度限制 500 字符 | Taste | P3 务实 | 大参数影响日志可读性 | 不限制 |
| 18 | Eng | application-prod.yml 不存在：修正 T003 | Mechanical | P3 务实 | 任务引用不存在的文件 | 创建空文件 |
| 19 | Eng | GlobalException 无上下文：注入 HttpServletRequest | Mechanical | P1 完整性 | 异常日志缺少请求信息 | 仅依赖 MDC |

## GSTACK REVIEW REPORT

| Review | Trigger | Why | Runs | Status | Findings |
|--------|---------|-----|------|--------|----------|
| CEO Review | `/plan-ceo-review` | Scope & strategy | 1 | issues_open | 12 findings (3C/5H/4M), mode: SELECTIVE_EXPANSION, 0 proposals, 0 accepted, 0 deferred |
| Codex Review | `/codex review` | Independent 2nd opinion | 0 | — | Codex unavailable (region 403) |
| Eng Review | `/plan-eng-review` | Architecture & tests (required) | 1 | issues_open | 13 findings (2C/3H/5M/3L), 5 critical gaps |
| Design Review | `/plan-design-review` | UI/UX gaps | 0 | — | Skipped, no UI scope |
| DX Review | `/plan-devex-review` | Developer experience gaps | 0 | — | Skipped, no DX scope |

**UNRESOLVED:** 0 unresolved decisions (all auto-decided via 6 principles)
**VERDICT:** CEO + ENG reviews completed with issues_open — critical gaps must be fixed before implementation

### Critical Gaps (must fix before implementation)

1. **@Async MDC 丢失** — NotificationService 使用 @Async，异步线程中 MDC 为空，日志无法串联
2. **AOP 前缀遗漏关键操作** — login/register/adminLogin/changeTable/staffCancelReservation 未被拦截
3. **`*code*` 脱敏误杀** — couponCode/qrCode/reservationCode 等非敏感参数被完全隐藏
4. **未配置 AsyncAppender** — 声称异步写入但实际同步，高负载下阻塞请求线程
5. **AOP+事务代理顺序** — BusinessLogAspect 可能在事务提交前记录"成功"，日志与数据不一致

### High Priority Fixes

6. **无测试任务** — 日志系统横切所有路径，必须添加单元测试和集成测试
7. **Token 失败类型无法区分** — AuthInterceptor 统一 catch Exception，无法区分过期/无效
8. **拦截器顺序未指定** — LoggingInterceptor 需要在 AuthInterceptor 之前执行
9. **GlobalExceptionHandler 无请求上下文** — 异常日志缺少路径和用户信息
10. **checkNoShow 写操作被跳过** — check 前缀在跳过列表中，黑名单操作无审计

### Taste Decisions (surfaced for user)

- **注解 vs 前缀匹配**：采用 @BusinessLog + 前缀匹配混合方案（P5 显式优于巧妙）
- **JSON vs 纯文本**：保留纯文本格式，添加 JSON Appender 作为可选 Profile（P6 行动优先）
- **安全审计双写**：添加 security_audit_log 数据库表（P1+P2 沸湖）
- **参数截断**：添加 500 字符最大长度限制（P3 务实）
