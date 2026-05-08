# Research: Business Logging System

**Feature**: 003-business-logging
**Date**: 2026-05-08

## 1. Logback 文件输出与滚动策略

**Decision**: 使用 `logback-spring.xml` 配置文件，定义控制台 + 主日志文件 + 错误日志文件三个 Appender。

**Rationale**:
- Spring Boot 默认使用 Logback，`logback-spring.xml` 是官方推荐的配置方式，支持 Spring Profile 区分环境
- 滚动策略使用 `SizeAndTimeBasedRollingPolicy`，同时按天和按大小滚动，是生产环境最佳实践
- 错误日志单独输出到 `agritainment-error.log`，方便运维快速定位问题
- 主日志保留 30 天、错误日志保留 90 天，符合一般业务系统的审计需求

**Alternatives considered**:
- 仅使用 `application.yml` 配置：无法配置多 Appender、无法定制格式、无法区分错误日志文件
- 使用 Log4j2：需要额外依赖，项目已用 Logback，切换无必要
- 使用 JSON 结构化日志：当前为单机部署，不需要对接日志平台，纯文本更易阅读

## 2. AOP 切面拦截 Service 方法

**Decision**: 使用 Spring AOP `@Around` 通知拦截 `com.agritainment.service..*Service.*(..)` 切点，通过方法名前缀区分写操作和读操作。

**Rationale**:
- Spring AOP 是 Spring Boot 自带功能，无需额外依赖
- `@Around` 通知可以在方法执行前后都记录日志，且能捕获异常
- 方法名前缀匹配是业界常用做法，项目现有 Service 方法命名规范（createXxx、getXxx 等）完全适配
- 只拦截写操作避免读操作产生大量噪音日志

**Alternatives considered**:
- 拦截 Controller 层：Controller 只做参数校验和调用 Service，业务逻辑在 Service 层，拦截 Service 更有意义
- 自定义注解标记需要日志的方法：需要修改每个 Service 方法，侵入性高，且容易遗漏
- 拦截所有 Service 方法：读操作日志量大且价值低，会产生噪音

**方法名前缀匹配规则**:

| 类型 | 前缀 | 处理 |
|------|------|------|
| 写操作 | create*, save*, update*, delete*, remove*, cancel*, settle*, rent*, purchase*, grant*, transfer*, verify*, refund*, checkin*, bind*, share*, unshare*, send*, control*, complete* | 记录日志 |
| 读操作 | get*, find*, list*, query*, check*, is*, has*, count* | 跳过 |

**特殊情况处理**:
- `checkNoShow`（UserService）：以 check 开头但属于写操作（会增加 no-show 计数），需要特殊处理
- `incrementNoShow`（UserService）：以 increment 开头，不在默认写操作列表中，需要添加
- `generateIdentityCode`/`generateCouponCode`（UserService）：以 generate 开头，属于工具方法，不需要记录
- `code2Session`/`getAccessToken`/`sendSubscribeMessage`（WechatService）：已有日志，AOP 切面会重复记录，需要排除 WechatService

**Decision**: 在切面中排除 `WechatService` 和 `ScheduledTaskService`（它们已有自己的日志逻辑），对 `UserService` 的 `checkNoShow` 和 `incrementNoShow` 添加 `increment*` 到写操作前缀列表。

## 3. MDC 请求 ID 串联

**Decision**: 在 LoggingInterceptor 的 `preHandle` 中生成 UUID 作为 requestId，注入 MDC；在 `afterCompletion` 中清理 MDC。

**Rationale**:
- MDC（Mapped Diagnostic Context）是 SLF4J 提供的线程级上下文机制，Logback 格式配置中可直接引用 `%X{key}`
- 在拦截器中统一注入，所有后续日志（包括 AOP 切面、Service 手动日志）自动携带 requestId
- 在 `afterCompletion` 中清理避免线程池复用导致的 MDC 污染

**MDC 字段**:

| Key | 来源 | 用途 |
|-----|------|------|
| requestId | UUID 生成 | 串联同一请求的所有日志 |
| userId | AuthInterceptor 注入的 request attribute | 标识操作人 |
| role | AuthInterceptor 注入的 request attribute | 标识操作角色 |

**Logback 格式配置**:
```
%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{requestId:-}] [%X{userId:-}] %logger{36} - %msg%n
```

## 4. 敏感数据脱敏

**Decision**: 创建 `SensitiveDataUtils` 工具类，在 AOP 切面序列化参数时调用脱敏方法。

**Rationale**:
- 集中管理脱敏规则，避免散落在各处
- AOP 切面统一调用，业务代码无需关心脱敏逻辑

**脱敏规则**:

| 参数类型 | 脱敏方式 | 示例 |
|---------|---------|------|
| phone | 中间4位替换为 **** | 138****8899 |
| openid | 保留前4位和后4位，中间用 ... | oX2b...k9Lm |
| identityCode | 保留前4位和后4位，中间用 ... | ABCD...WXYZ |
| password | 完全隐藏 | ****** |
| code（验证码） | 完全隐藏 | ****** |

**实现方式**: 通过参数名匹配判断是否需要脱敏，参数名包含 `phone`/`openid`/`identityCode`/`password`/`code` 时自动脱敏。

## 5. 请求访问日志实现

**Decision**: 使用 Servlet Filter（`RequestLoggingFilter`）记录请求耗时，使用 HandlerInterceptor（`LoggingInterceptor`）注入 MDC 上下文。

**Rationale**:
- Filter 在 Servlet 层面工作，能记录所有请求（包括静态资源），适合记录耗时
- Interceptor 在 Spring MVC 层面工作，能获取 Handler 信息和用户上下文，适合注入 MDC
- 两者配合：Filter 记录耗时，Interceptor 注入上下文，职责清晰

**排除路径**: `/uploads/**`（静态资源不需要访问日志）

**慢请求阈值**: 3 秒，超过标记 `[SLOW]`

## 6. 安全审计日志实现

**Decision**: 在现有 AuthInterceptor 和 RoleInterceptor 中增加日志语句，不创建新的拦截器。

**Rationale**:
- 认证和鉴权逻辑已在现有拦截器中，直接在拒绝点加日志最直接
- 创建新拦截器会增加复杂度，且需要处理拦截器顺序问题
- IP 获取使用 `X-Forwarded-For` → `X-Real-IP` → `remoteAddr` 的优先级链

## 7. 环境区分

**Decision**: 使用 `logback-spring.xml` 的 `<springProfile>` 标签区分开发和生产环境。

**Rationale**:
- 开发环境：DEBUG 级别，输出到控制台 + 文件
- 生产环境：INFO 级别，仅输出到文件
- 移除 `application.yml` 和 `application-prod.yml` 中的 `logging.level` 配置，避免配置分散

## 8. Spring AOP 依赖确认

**Decision**: `spring-boot-starter-web` 已包含 `spring-aop`，无需额外添加依赖。但需要确认 `spring-boot-starter-aop` 是否需要显式引入以启用 `@EnableAspectJAutoProxy`。

**Rationale**:
- Spring Boot 3.x 中，如果 classpath 上有 `spring-aop` 和 AspectJ，AOP 自动配置会生效
- 但 `@Aspect` 注解需要 AspectJ Weaver，`spring-boot-starter-web` 不包含它
- 需要在 pom.xml 中添加 `spring-boot-starter-aop` 依赖

**修正**: 需要添加 `spring-boot-starter-aop` 依赖到 pom.xml。这是 Spring Boot 官方 starter，不属于"引入新框架"。
