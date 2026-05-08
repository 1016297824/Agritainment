# Feature Specification: Business Logging System

**Feature Branch**: `003-business-logging`
**Created**: 2026-05-08
**Status**: Draft
**Input**: User description: "增加一个全面的业务日志系统"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 运维人员排查线上问题 (Priority: P1)

运维人员收到线上异常告警后，需要通过日志快速定位问题根因。当前系统日志仅输出到控制台、核心业务操作无日志记录，导致进程重启后日志丢失、无法追溯用户操作历史。

**Why this priority**: 日志最核心的价值是排查线上问题，没有持久化的日志文件和业务操作记录，线上问题完全无法排查。

**Independent Test**: 可以通过触发一个业务操作（如创建预约），然后检查日志文件中是否出现对应的业务日志来验证。

**Acceptance Scenarios**:

1. **Given** 系统正在运行，**When** 用户执行业务操作（登录、预约、点餐等），**Then** 对应的业务操作日志被写入日志文件，包含操作人、操作类型、关键参数和操作结果
2. **Given** 系统发生异常，**When** 请求处理过程中抛出异常，**Then** 异常日志被写入错误日志文件，包含请求路径、用户ID和完整异常堆栈
3. **Given** 系统重启，**When** 运维人员查看历史日志，**Then** 重启前的日志仍然存在于日志文件中

---

### User Story 2 - 安全事件审计 (Priority: P2)

安全管理员需要审计系统中的安全事件，包括认证失败、权限不足、黑名单拦截等。当前拦截器在认证/鉴权失败时不记录任何日志，安全事件无审计痕迹。

**Why this priority**: 安全审计是日志的重要功能，但相比基础业务日志，其紧迫性稍低。

**Independent Test**: 可以通过使用无效 Token 访问受保护接口，然后检查日志中是否出现安全审计日志来验证。

**Acceptance Scenarios**:

1. **Given** 用户携带无效 Token 请求受保护接口，**When** 认证拦截器拒绝请求，**Then** 安全日志记录 Token 无效事件，包含请求 IP 和路径
2. **Given** 普通用户请求管理员接口，**When** 角色拦截器拒绝请求，**Then** 安全日志记录权限不足事件，包含用户角色和所需角色
3. **Given** 被拉黑用户尝试操作，**When** 业务层检测到黑名单状态，**Then** 安全日志记录黑名单拦截事件

---

### User Story 3 - 请求访问监控 (Priority: P3)

运维人员需要监控系统的 HTTP 请求情况，包括请求量、响应时间、错误率等。当前系统无任何请求访问日志。

**Why this priority**: 请求访问日志对性能监控和流量分析有价值，但相比业务日志和安全日志，优先级较低。

**Independent Test**: 可以通过发送多个 HTTP 请求，然后检查日志文件中是否出现对应的访问日志（含耗时）来验证。

**Acceptance Scenarios**:

1. **Given** 系统正在运行，**When** 任何 HTTP 请求到达，**Then** 访问日志记录请求方法、路径、响应状态码和耗时
2. **Given** 慢请求（超过3秒），**When** 请求处理完成，**Then** 访问日志额外标记慢请求标识
3. **Given** 请求返回 4xx/5xx 状态码，**When** 日志记录时，**Then** 使用 WARN/ERROR 级别而非 INFO

---

### Edge Cases

- 同一请求的多条日志如何串联？通过请求 ID（requestId）在 MDC 中传递，所有日志行自动携带
- 敏感数据（手机号、openid、验证码）如何在日志中脱敏？通过脱敏工具类统一处理
- 日志文件过大如何处理？通过滚动策略按天+按大小归档，自动压缩旧日志
- 读操作（查询列表等）是否需要记录日志？不需要，避免产生大量噪音日志
- AOP 切面拦截 Service 方法时，如何区分写操作和读操作？通过方法名前缀匹配规则区分

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统必须将日志持久化写入本地文件，而非仅输出到控制台
- **FR-002**: 系统必须配置日志滚动策略，按天归档、单文件最大 50MB、主日志保留 30 天、错误日志保留 90 天、总大小上限 1GB
- **FR-003**: 系统必须记录所有写操作的业务日志（create/update/delete/cancel/settle/rent/purchase/grant/transfer/verify/refund/checkin/bind/share 等），包含操作人 ID、操作类型、关键参数和操作结果
- **FR-004**: 系统不得记录读操作的日志（get/list/query/find 等），避免产生噪音
- **FR-005**: 系统必须记录 HTTP 请求访问日志，包含请求方法、路径、响应状态码、耗时和用户 ID
- **FR-006**: 系统必须记录安全审计日志，包含 Token 验证失败、角色权限不足、黑名单拦截事件
- **FR-007**: 系统必须记录异常日志，包含请求路径、用户 ID 和完整异常堆栈
- **FR-008**: 系统必须对敏感数据脱敏：手机号中间4位替换为 ****，openid 和身份码保留首尾各4位，密码和验证码完全隐藏
- **FR-009**: 系统必须通过请求 ID（requestId）串联同一请求的多条日志
- **FR-010**: 系统必须在慢请求（超过3秒）的访问日志中额外标记慢请求标识
- **FR-011**: 系统必须将 4xx 响应的访问日志标记为 WARN 级别，5xx 标记为 ERROR 级别
- **FR-012**: 系统必须区分开发环境和生产环境的日志级别：开发环境 DEBUG，生产环境 INFO
- **FR-013**: 系统必须将错误日志（WARN 及以上）单独输出到错误日志文件

### Key Entities

- **Access Log**: HTTP 请求访问记录，包含请求方法、路径、状态码、耗时、用户 ID、请求 ID
- **Business Log**: 业务操作记录，包含操作人 ID、操作类型、方法名、关键参数、操作结果
- **Security Log**: 安全审计记录，包含事件类型、用户 ID、请求 IP、请求路径、角色信息
- **Error Log**: 异常记录，包含请求路径、用户 ID、异常类型、异常消息、异常堆栈

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 任何业务写操作发生后，对应的业务日志在 1 秒内出现在日志文件中
- **SC-002**: 进程重启后，历史日志文件完整保留，无丢失
- **SC-003**: 运维人员通过 requestId 可在日志文件中检索到同一请求的全部关联日志
- **SC-004**: 敏感数据（手机号、密码、验证码）在日志中不可见原始值
- **SC-005**: 日志文件自动归档，单文件不超过 50MB，磁盘占用不超过 1GB
- **SC-006**: 安全事件（认证失败、权限不足）100% 被记录到日志文件

## Assumptions

- 项目为单机部署，日志写入本地文件即可满足需求，暂不需要对接 ELK/Loki 等日志平台
- 使用项目已有的 SLF4J + Logback 日志框架，不引入新依赖
- 使用 Spring AOP 切面自动拦截 Service 方法，减少手动日志代码
- 日志格式为人类可读的纯文本格式，非结构化 JSON
- 前端日志不在本次范围内，仅覆盖后端日志
- 已有的 ScheduledTaskService、WechatService、IndexInitializer 日志保持现状不改动
