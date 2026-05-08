# Data Model: Business Logging System

**Feature**: 003-business-logging
**Date**: 2026-05-08

## 概述

日志系统不引入新的数据库实体。所有日志数据通过 Logback 写入本地文件系统。数据模型描述的是日志事件的结构，而非持久化实体。

## 日志事件结构

### AccessLogEvent（请求访问日志）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| requestId | String | MDC | 请求唯一标识 |
| userId | Long | MDC | 操作人 ID（未登录为 -） |
| method | String | HttpServletRequest | HTTP 方法 |
| path | String | HttpServletRequest | 请求路径 |
| statusCode | int | HttpServletResponse | 响应状态码 |
| duration | long | Filter 计算 | 请求耗时（ms） |

**状态规则**:
- 2xx/3xx → INFO
- 4xx → WARN
- 5xx → ERROR
- duration > 3000ms → 额外标记 [SLOW]

### BusinessLogEvent（业务操作日志）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| requestId | String | MDC | 请求唯一标识 |
| userId | Long | MDC | 操作人 ID |
| className | String | AOP 切面 | Service 类名 |
| methodName | String | AOP 切面 | 方法名 |
| params | String | AOP 切面 | 脱敏后的关键参数 |
| result | String | AOP 切面 | 操作结果摘要 |
| error | String | AOP 切面 | 异常消息（失败时） |

**方法名匹配规则**:
- 写操作前缀：create*, save*, update*, delete*, remove*, cancel*, settle*, rent*, purchase*, grant*, transfer*, verify*, refund*, checkin*, bind*, share*, unshare*, send*, control*, complete*, increment*
- 读操作前缀（跳过）：get*, find*, list*, query*, check*, is*, has*, count*
- 排除类：WechatService, ScheduledTaskService

### SecurityLogEvent（安全审计日志）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| requestId | String | MDC | 请求唯一标识 |
| eventType | String | 拦截器 | 事件类型 |
| userId | Long | MDC/JWT | 用户 ID（可从 token 提取） |
| role | String | MDC | 用户角色 |
| requiredRole | String | @RequireRole | 所需角色（权限不足时） |
| clientIp | String | HttpServletRequest | 客户端 IP |
| path | String | HttpServletRequest | 请求路径 |
| reason | String | 拦截器 | 拒绝原因 |

**事件类型**:
- TOKEN_MISSING: Token 缺失
- TOKEN_EXPIRED: Token 过期
- TOKEN_INVALID: Token 无效
- BLACKLISTED: 黑名单拦截
- ROLE_DENIED: 权限不足

### ErrorLogEvent（异常日志）

| 字段 | 类型 | 来源 | 说明 |
|------|------|------|------|
| requestId | String | MDC | 请求唯一标识 |
| userId | Long | MDC | 操作人 ID |
| path | String | HttpServletRequest | 请求路径 |
| method | String | HttpServletRequest | HTTP 方法 |
| errorCode | String | AppException | 业务错误码 |
| errorMessage | String | AppException/Exception | 错误消息 |
| stackTrace | String | Exception | 异常堆栈 |

## 脱敏规则

| 参数名模式 | 脱敏方式 | 示例 |
|-----------|---------|------|
| *phone* | 中间4位替换 **** | 138****8899 |
| *openid* | 保留前4后4，中间... | oX2b...k9Lm |
| *identityCode* | 保留前4后4，中间... | ABCD...WXYZ |
| *password* | 完全隐藏 | ****** |
| *code* | 完全隐藏 | ****** |

## 日志文件结构

```
./logs/
├── agritainment.log                    # 全量日志（INFO+）
├── agritainment-error.log              # 错误日志（WARN+）
├── agritainment-2026-05-07.log.gz      # 归档（自动压缩）
└── agritainment-error-2026-05-07.log.gz
```

**滚动策略**:
- 主日志：按天 + 按大小（50MB），保留 30 天，总上限 1GB
- 错误日志：按天 + 按大小（50MB），保留 90 天，总上限 1GB
