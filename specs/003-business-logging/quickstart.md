# Quickstart: Business Logging System

**Feature**: 003-business-logging
**Date**: 2026-05-08

## 前置条件

- Java 21
- Maven 3.6+
- 项目已能正常启动

## 开发步骤

### 1. 添加 AOP 依赖

在 `server/pom.xml` 中添加：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

### 2. 创建 Logback 配置

创建 `server/src/main/resources/logback-spring.xml`，配置：
- 控制台 Appender（开发环境）
- 文件 Appender（主日志 + 错误日志）
- 滚动策略
- Spring Profile 区分

### 3. 创建脱敏工具类

创建 `SensitiveDataUtils.java`，实现手机号、openid、身份码、密码、验证码的脱敏。

### 4. 创建请求日志组件

- `LoggingInterceptor.java`：注入 MDC（requestId、userId、role）
- `RequestLoggingFilter.java`：记录请求耗时和状态码

### 5. 创建业务日志切面

- `BusinessLogAspect.java`：AOP 切面拦截 Service 写操作

### 6. 增强安全审计日志

- 修改 `AuthInterceptor.java`：增加 Token 验证失败日志
- 修改 `RoleInterceptor.java`：增加权限不足日志

### 7. 增强异常日志

- 修改 `GlobalExceptionHandler.java`：补充请求上下文

### 8. 注册组件

- 修改 `WebConfig.java`：注册 LoggingInterceptor
- 修改 `application.yml` / `application-prod.yml`：移除 logging.level

## 验证方式

1. 启动应用
2. 触发业务操作（如创建预约）
3. 检查 `./logs/agritainment.log` 中是否出现对应的业务日志
4. 检查日志中 requestId 是否一致
5. 检查手机号等敏感数据是否已脱敏
6. 使用无效 Token 访问接口，检查安全审计日志
7. 触发异常，检查错误日志文件

## 日志示例

```
2026-05-08 14:30:00.123 [http-nio-8080-exec-1] INFO  [a1b2c3d4] [3] c.a.l.RequestLoggingFilter - [ACCESS] POST /api/v1/dining/reservations 201 89ms
2026-05-08 14:30:00.120 [http-nio-8080-exec-1] INFO  [a1b2c3d4] [3] c.a.l.BusinessLogAspect - [BIZ] createReservation userId=3, tableId=5, date=2026-05-08, timeSlot=lunch → reservationId=12
2026-05-08 14:30:05.456 [http-nio-8080-exec-2] WARN  [e5f6g7h8] [-] c.a.i.AuthInterceptor - [SEC] TOKEN_INVALID IP=192.168.1.100, path=/api/v1/dining/reservations
```
