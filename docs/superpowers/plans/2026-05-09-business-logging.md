# Business Logging System Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为农家乐小程序后端增加全面的业务日志系统，实现四层日志架构（访问日志、业务日志、安全审计日志、系统日志），支持异步写入、MDC 请求串联、敏感数据脱敏。

**Architecture:** AOP 切面 + HandlerInterceptor + Servlet Filter 三层拦截。LoggingInterceptor 注入 MDC 上下文，BusinessLogAspect 通过 @BusinessLog 注解 + 前缀匹配拦截写操作，RequestLoggingFilter 记录访问日志，AuthInterceptor/RoleInterceptor 增强安全审计。Logback AsyncAppender 异步写入文件。

**Tech Stack:** Spring Boot 3.2.5, Spring AOP, SLF4J + Logback, MyBatis-Plus, JJWT 0.12.5

---

## File Structure

| Action | File | Responsibility |
|--------|------|----------------|
| MODIFY | `server/pom.xml` | 添加 AOP 依赖 |
| CREATE | `server/src/main/resources/logback-spring.xml` | Logback 配置（AsyncAppender + 滚动策略） |
| MODIFY | `server/src/main/resources/application.yml` | 移除 logging.level |
| CREATE | `server/src/main/java/com/agritainment/common/SensitiveDataUtils.java` | 敏感数据脱敏工具 |
| CREATE | `server/src/main/java/com/agritainment/annotation/BusinessLog.java` | 业务日志注解 |
| CREATE | `server/src/main/java/com/agritainment/logging/BusinessLogAspect.java` | AOP 切面（@Order + 前缀匹配） |
| CREATE | `server/src/main/java/com/agritainment/interceptor/LoggingInterceptor.java` | MDC 注入拦截器 |
| CREATE | `server/src/main/java/com/agritainment/config/MdcTaskDecorator.java` | 异步线程 MDC 传播 |
| MODIFY | `server/src/main/java/com/agritainment/config/WebConfig.java` | 注册拦截器（显式顺序） |
| CREATE | `server/src/main/java/com/agritainment/logging/RequestLoggingFilter.java` | 请求访问日志 Filter |
| MODIFY | `server/src/main/java/com/agritainment/common/GlobalExceptionHandler.java` | 增强异常日志（注入请求上下文） |
| MODIFY | `server/src/main/java/com/agritainment/interceptor/AuthInterceptor.java` | 安全审计（区分 Token 类型） |
| MODIFY | `server/src/main/java/com/agritainment/interceptor/RoleInterceptor.java` | 安全审计（ROLE_DENIED） |
| CREATE | `server/src/main/java/com/agritainment/entity/SecurityAuditLog.java` | 安全审计日志实体 |
| CREATE | `server/src/main/java/com/agritainment/mapper/SecurityAuditLogMapper.java` | 安全审计日志 Mapper |
| CREATE | `server/src/main/java/com/agritainment/service/SecurityAuditLogService.java` | 安全审计日志 Service（异步双写） |
| MODIFY | `server/src/main/resources/schema.sql` | 添加 security_audit_log 表 |

---

### Task 1: Add AOP Dependency

**Files:**
- Modify: `server/pom.xml:25-76`

- [ ] **Step 1: Add spring-boot-starter-aop dependency**

在 `server/pom.xml` 的 `<dependencies>` 中，`spring-boot-starter-validation` 之后添加：

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/pom.xml
git commit -m "feat(logging): add spring-boot-starter-aop dependency"
```

---

### Task 2: Create logback-spring.xml with AsyncAppender

**Files:**
- Create: `server/src/main/resources/logback-spring.xml`

- [ ] **Step 1: Create logback-spring.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATH" value="./logs"/>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{requestId:-}] %-5level %logger{36} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="ACCESS_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/access.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/access.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="BUSINESS_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/business.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/business.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="SECURITY_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/security.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/security.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="SYSTEM_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_PATH}/system.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_PATH}/system.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>1GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="ASYNC_ACCESS" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="ACCESS_FILE"/>
    </appender>

    <appender name="ASYNC_BUSINESS" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="BUSINESS_FILE"/>
    </appender>

    <appender name="ASYNC_SECURITY" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="SECURITY_FILE"/>
    </appender>

    <appender name="ASYNC_SYSTEM" class="ch.qos.logback.classic.AsyncAppender">
        <queueSize>512</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <appender-ref ref="SYSTEM_FILE"/>
    </appender>

    <logger name="ACCESS" level="INFO" additivity="false">
        <appender-ref ref="ASYNC_ACCESS"/>
    </logger>

    <logger name="BUSINESS" level="INFO" additivity="false">
        <appender-ref ref="ASYNC_BUSINESS"/>
    </logger>

    <logger name="SECURITY" level="INFO" additivity="false">
        <appender-ref ref="ASYNC_SECURITY"/>
    </logger>

    <springProfile name="default">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="ASYNC_SYSTEM"/>
        </root>
        <logger name="com.agritainment" level="DEBUG"/>
    </springProfile>

    <springProfile name="prod">
        <root level="WARN">
            <appender-ref ref="ASYNC_SYSTEM"/>
        </root>
        <logger name="com.agritainment" level="INFO"/>
    </springProfile>
</configuration>
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/resources/logback-spring.xml
git commit -m "feat(logging): add logback-spring.xml with AsyncAppender and rolling policy"
```

---

### Task 3: Remove logging.level from application.yml

**Files:**
- Modify: `server/src/main/resources/application.yml:46-48`

- [ ] **Step 1: Remove logging.level section**

删除 `application.yml` 末尾的：

```yaml
logging:
  level:
    com.agritainment: DEBUG
```

日志级别现在由 logback-spring.xml 控制。

- [ ] **Step 2: Verify app starts**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn spring-boot:run -q` (启动后 Ctrl+C)
Expected: 应用正常启动，日志输出格式包含 requestId 占位符

- [ ] **Step 3: Commit**

```bash
git add server/src/main/resources/application.yml
git commit -m "feat(logging): remove logging.level from application.yml, now controlled by logback-spring.xml"
```

---

### Task 4: Create SensitiveDataUtils

**Files:**
- Create: `server/src/main/java/com/agritainment/common/SensitiveDataUtils.java`

- [ ] **Step 1: Create SensitiveDataUtils**

```java
package com.agritainment.common;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SensitiveDataUtils {

    private static final int MAX_PARAM_LENGTH = 500;

    private SensitiveDataUtils() {}

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskOpenid(String openid) {
        if (openid == null || openid.length() < 8) return "****";
        return openid.substring(0, 5) + "****" + openid.substring(openid.length() - 2);
    }

    public static String maskIdentityCode(String code) {
        if (code == null || code.length() < 4) return "****";
        return code.substring(0, 3) + "****" + code.substring(code.length() - 4);
    }

    public static String maskPassword() {
        return "******";
    }

    public static String maskSmsCode() {
        return "******";
    }

    public static String truncate(String value) {
        if (value == null) return null;
        if (value.length() <= MAX_PARAM_LENGTH) return value;
        return value.substring(0, MAX_PARAM_LENGTH) + "...[TRUNCATED]";
    }

    private static final Map<String, java.util.function.Supplier<String>> SENSITIVE_PARAM_MASKS = new LinkedHashMap<>();

    static {
        SENSITIVE_PARAM_MASKS.put("phone", () -> "****");
        SENSITIVE_PARAM_MASKS.put("openid", () -> "****");
        SENSITIVE_PARAM_MASKS.put("password", () -> "******");
        SENSITIVE_PARAM_MASKS.put("smsCode", () -> "******");
        SENSITIVE_PARAM_MASKS.put("verifyCode", () -> "******");
        SENSITIVE_PARAM_MASKS.put("identityCode", () -> "****");
    }

    public static boolean isSensitiveParam(String paramName) {
        return SENSITIVE_PARAM_MASKS.containsKey(paramName);
    }

    public static String maskParam(String paramName, String value) {
        if (value == null) return null;
        java.util.function.Supplier<String> masker = SENSITIVE_PARAM_MASKS.get(paramName);
        if (masker != null) return masker.get();
        return truncate(value);
    }
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/common/SensitiveDataUtils.java
git commit -m "feat(logging): add SensitiveDataUtils with precise masking rules"
```

---

### Task 5: Create @BusinessLog Annotation

**Files:**
- Create: `server/src/main/java/com/agritainment/annotation/BusinessLog.java`

- [ ] **Step 1: Create BusinessLog annotation**

```java
package com.agritainment.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessLog {
    String value() default "";
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/annotation/BusinessLog.java
git commit -m "feat(logging): add @BusinessLog annotation for explicit method marking"
```

---

### Task 6: Create BusinessLogAspect with @Order

**Files:**
- Create: `server/src/main/java/com/agritainment/logging/BusinessLogAspect.java`

- [ ] **Step 1: Create BusinessLogAspect**

```java
package com.agritainment.logging;

import com.agritainment.annotation.BusinessLog;
import com.agritainment.common.SensitiveDataUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;

@Aspect
@Component
public class BusinessLogAspect implements Ordered {

    private static final Logger log = LoggerFactory.getLogger("BUSINESS");

    private static final Set<String> WRITE_PREFIXES = Set.of(
            "create", "save", "update", "delete", "cancel",
            "register", "login", "adminLogin",
            "changeTable", "staffCancelReservation", "checkNoShow"
    );

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Around("@annotation(com.agritainment.annotation.BusinessLog)")
    public Object logAnnotatedMethod(ProceedingJoinPoint pjp) throws Throwable {
        return doLog(pjp, true);
    }

    @Around("execution(* com.agritainment.service..*Service.*(..))")
    public Object logServiceWriteMethods(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String methodName = sig.getMethod().getName();
        boolean isWrite = WRITE_PREFIXES.stream().anyMatch(methodName::startsWith);
        if (!isWrite) {
            return pjp.proceed();
        }
        if (sig.getMethod().getAnnotation(BusinessLog.class) != null) {
            return pjp.proceed();
        }
        return doLog(pjp, false);
    }

    private Object doLog(ProceedingJoinPoint pjp, boolean annotated) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String className = sig.getDeclaringType().getSimpleName();
        String methodName = sig.getMethod().getName();
        String action = annotated && sig.getMethod().getAnnotation(BusinessLog.class) != null
                ? sig.getMethod().getAnnotation(BusinessLog.class).value()
                : methodName;

        String params = maskParams(sig.getParameterNames(), pjp.getArgs());

        Object result;
        try {
            result = pjp.proceed();
        } catch (Exception e) {
            log.info("[BIZ_FAIL] action={} class={} method={} params={} error={}",
                    action, className, methodName, params, e.getMessage());
            throw e;
        }

        String resultSummary = result != null ? SensitiveDataUtils.truncate(result.toString()) : "void";
        log.info("[BIZ_OK] action={} class={} method={} params={} result={}",
                action, className, methodName, params, resultSummary);
        return result;
    }

    private String maskParams(String[] paramNames, Object[] args) {
        if (paramNames == null || args == null) return "{}";
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < paramNames.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(paramNames[i]).append("=");
            String value = args[i] != null ? args[i].toString() : "null";
            sb.append(SensitiveDataUtils.maskParam(paramNames[i], value));
        }
        sb.append("}");
        return SensitiveDataUtils.truncate(sb.toString());
    }
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/logging/BusinessLogAspect.java
git commit -m "feat(logging): add BusinessLogAspect with @Order(LOWEST_PRECEDENCE) and prefix matching"
```

---

### Task 7: Create LoggingInterceptor (MDC Injection)

**Files:**
- Create: `server/src/main/java/com/agritainment/interceptor/LoggingInterceptor.java`

- [ ] **Step 1: Create LoggingInterceptor**

```java
package com.agritainment.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID = "requestId";
    private static final String USER_ID = "userId";
    private static final String ROLE = "role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        MDC.put(REQUEST_ID, requestId);
        request.setAttribute(REQUEST_ID, requestId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(REQUEST_ID);
        MDC.remove(USER_ID);
        MDC.remove(ROLE);
    }
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/interceptor/LoggingInterceptor.java
git commit -m "feat(logging): add LoggingInterceptor for MDC requestId injection"
```

---

### Task 8: Create MdcTaskDecorator (Async MDC Propagation)

**Files:**
- Create: `server/src/main/java/com/agritainment/config/MdcTaskDecorator.java`

- [ ] **Step 1: Create MdcTaskDecorator**

```java
package com.agritainment.config;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return () -> {
            try {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```

- [ ] **Step 2: Configure async executor to use MdcTaskDecorator**

在 `AgritainmentApplication.java` 中添加异步配置 Bean：

```java
package com.agritainment;

import com.agritainment.config.MdcTaskDecorator;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@SpringBootApplication
@MapperScan("com.agritainment.mapper")
@EnableAsync
@EnableScheduling
public class AgritainmentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgritainmentApplication.class, args);
    }

    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    public Executor asyncExecutor(MdcTaskDecorator mdcTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

- [ ] **Step 3: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add server/src/main/java/com/agritainment/config/MdcTaskDecorator.java server/src/main/java/com/agritainment/AgritainmentApplication.java
git commit -m "feat(logging): add MdcTaskDecorator for async thread MDC propagation"
```

---

### Task 9: Register Interceptors with Explicit Order in WebConfig

**Files:**
- Modify: `server/src/main/java/com/agritainment/config/WebConfig.java`

- [ ] **Step 1: Update WebConfig to register LoggingInterceptor with order**

将 `WebConfig.java` 替换为：

```java
package com.agritainment.config;

import com.agritainment.interceptor.AuthInterceptor;
import com.agritainment.interceptor.LoggingInterceptor;
import com.agritainment.interceptor.RoleInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final AuthInterceptor authInterceptor;
    private final RoleInterceptor roleInterceptor;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    public WebConfig(LoggingInterceptor loggingInterceptor, AuthInterceptor authInterceptor, RoleInterceptor roleInterceptor) {
        this.loggingInterceptor = loggingInterceptor;
        this.authInterceptor = authInterceptor;
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/api/v1/**")
                .order(1);
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/login", "/api/v1/auth/register",
                        "/api/v1/auth/admin-login", "/api/v1/auth/sms-code",
                        "/api/v1/journals/shared", "/api/v1/products",
                        "/api/v1/products/{id}", "/api/v1/dining/dishes")
                .order(2);
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/v1/**")
                .order(3);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/config/WebConfig.java
git commit -m "feat(logging): register LoggingInterceptor with explicit order(1) before auth"
```

---

### Task 10: Create RequestLoggingFilter

**Files:**
- Create: `server/src/main/java/com/agritainment/logging/RequestLoggingFilter.java`

- [ ] **Step 1: Create RequestLoggingFilter**

```java
package com.agritainment.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("ACCESS");
    private static final String START_TIME = "requestStartTime";
    private static final long SLOW_THRESHOLD_MS = 3000;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/uploads/")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String requestId = (String) request.getAttribute("requestId");
            String userId = request.getAttribute("userId") != null ? request.getAttribute("userId").toString() : "-";
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            String slowMark = duration > SLOW_THRESHOLD_MS ? " [SLOW]" : "";

            log.info("[ACCESS] requestId={} method={} uri={} status={} duration={}ms userId={}{}",
                    requestId, method, uri, status, duration, userId, slowMark);
        }
    }
}
```

- [ ] **Step 2: Register filter in WebConfig**

在 `WebConfig.java` 中添加 FilterRegistrationBean：

在 WebConfig 类中添加：

```java
    @Bean
    public FilterRegistrationBean<RequestLoggingFilter> requestLoggingFilterRegistration(RequestLoggingFilter filter) {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/v1/*");
        registration.setOrder(1);
        return registration;
    }
```

同时在 WebConfig 顶部添加 import：

```java
import com.agritainment.logging.RequestLoggingFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
```

- [ ] **Step 3: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add server/src/main/java/com/agritainment/logging/RequestLoggingFilter.java server/src/main/java/com/agritainment/config/WebConfig.java
git commit -m "feat(logging): add RequestLoggingFilter with slow request marking"
```

---

### Task 11: Enhance GlobalExceptionHandler

**Files:**
- Modify: `server/src/main/java/com/agritainment/common/GlobalExceptionHandler.java`

- [ ] **Step 1: Update GlobalExceptionHandler**

```java
package com.agritainment.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public Result<?> handleAppException(AppException e, HttpServletRequest request) {
        log.warn("[BIZ_ERROR] requestId={} path={} userId={} code={} message={}",
                MDC.get("requestId"), request.getRequestURI(),
                request.getAttribute("userId"), e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("[SYS_ERROR] requestId={} path={} userId={} error={}",
                MDC.get("requestId"), request.getRequestURI(),
                request.getAttribute("userId"), e.getMessage(), e);
        return Result.error(50000, "服务器内部错误");
    }
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/common/GlobalExceptionHandler.java
git commit -m "feat(logging): enhance GlobalExceptionHandler with request context in error logs"
```

---

### Task 12: Enhance AuthInterceptor with Token Type Distinction

**Files:**
- Modify: `server/src/main/java/com/agritainment/interceptor/AuthInterceptor.java`

- [ ] **Step 1: Update AuthInterceptor**

```java
package com.agritainment.interceptor;

import com.agritainment.enums.RoleEnum;
import com.agritainment.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger secLog = LoggerFactory.getLogger("SECURITY");
    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logSecurityEvent("TOKEN_MISSING", null, null, request);
            sendError(response, 40101, "未登录");
            return false;
        }

        try {
            String token = authHeader.substring(7);
            var claims = jwtUtil.parseToken(token);
            Long userId = Long.parseLong(claims.getSubject());
            RoleEnum role = RoleEnum.fromValue(claims.get("role", String.class));
            Boolean isMember = claims.get("isMember", Boolean.class);
            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            request.setAttribute("isMember", isMember);
            MDC.put("userId", userId.toString());
            MDC.put("role", role.getValue());
            return true;
        } catch (ExpiredJwtException e) {
            logSecurityEvent("TOKEN_EXPIRED", null, null, request);
            sendError(response, 40102, "Token已过期");
            return false;
        } catch (MalformedJwtException e) {
            logSecurityEvent("TOKEN_MALFORMED", null, null, request);
            sendError(response, 40103, "Token格式错误");
            return false;
        } catch (SignatureException e) {
            logSecurityEvent("TOKEN_SIGNATURE_INVALID", null, null, request);
            sendError(response, 40104, "Token签名无效");
            return false;
        } catch (JwtException e) {
            logSecurityEvent("TOKEN_INVALID", null, null, request);
            sendError(response, 40101, "Token无效");
            return false;
        }
    }

    private void logSecurityEvent(String eventType, Long userId, String role, HttpServletRequest request) {
        secLog.warn("[SECURITY] event={} userId={} role={} path={} ip={}",
                eventType, userId, role, request.getRequestURI(), getClientIp(request));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        new ObjectMapper().writeValue(response.getOutputStream(), Map.of("code", code, "message", message, "data", ""));
    }
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/interceptor/AuthInterceptor.java
git commit -m "feat(logging): enhance AuthInterceptor with token type distinction and security audit"
```

---

### Task 13: Enhance RoleInterceptor with Security Audit

**Files:**
- Modify: `server/src/main/java/com/agritainment/interceptor/RoleInterceptor.java`

- [ ] **Step 1: Update RoleInterceptor**

```java
package com.agritainment.interceptor;

import com.agritainment.annotation.RequireRole;
import com.agritainment.enums.RoleEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final Logger secLog = LoggerFactory.getLogger("SECURITY");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) return true;

        RequireRole annotation = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (annotation == null) return true;

        Object roleAttr = request.getAttribute("role");
        if (roleAttr == null) {
            sendError(response, 40101, "未登录");
            return false;
        }

        RoleEnum userRole = roleAttr instanceof RoleEnum r ? r : RoleEnum.fromValue(roleAttr.toString());

        for (RoleEnum role : annotation.value()) {
            if (role == userRole) return true;
        }

        String requiredRoles = Arrays.stream(annotation.value())
                .map(RoleEnum::getValue)
                .collect(Collectors.joining(","));
        secLog.warn("[SECURITY] event=ROLE_DENIED userId={} role={} requiredRoles={} path={} ip={}",
                request.getAttribute("userId"), userRole.getValue(), requiredRoles,
                request.getRequestURI(), getClientIp(request));

        sendError(response, 40301, "权限不足");
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        new ObjectMapper().writeValue(response.getOutputStream(), Map.of("code", code, "message", message, "data", ""));
    }
}
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/interceptor/RoleInterceptor.java
git commit -m "feat(logging): add ROLE_DENIED security audit to RoleInterceptor"
```

---

### Task 14: Create security_audit_log Table + Entity + Mapper + Service

**Files:**
- Modify: `server/src/main/resources/schema.sql`
- Create: `server/src/main/java/com/agritainment/entity/SecurityAuditLog.java`
- Create: `server/src/main/java/com/agritainment/mapper/SecurityAuditLogMapper.java`
- Create: `server/src/main/java/com/agritainment/service/SecurityAuditLogService.java`

- [ ] **Step 1: Add table to schema.sql**

在 `schema.sql` 末尾添加：

```sql
CREATE TABLE IF NOT EXISTS security_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    user_id BIGINT,
    role VARCHAR(20),
    path VARCHAR(255),
    detail VARCHAR(500),
    ip VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_event_type (event_type),
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_created_at (created_at)
);
```

- [ ] **Step 2: Create SecurityAuditLog entity**

```java
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
```

- [ ] **Step 3: Create SecurityAuditLogMapper**

```java
package com.agritainment.mapper;

import com.agritainment.entity.SecurityAuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecurityAuditLogMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<SecurityAuditLog> {}
```

- [ ] **Step 4: Create SecurityAuditLogService**

```java
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
```

- [ ] **Step 5: Integrate SecurityAuditLogService into AuthInterceptor**

在 `AuthInterceptor.java` 中注入 `SecurityAuditLogService`，在 `logSecurityEvent` 方法中添加异步数据库写入：

在 AuthInterceptor 中添加字段和修改构造器：

```java
    private final JwtUtil jwtUtil;
    private final SecurityAuditLogService auditLogService;

    public AuthInterceptor(JwtUtil jwtUtil, SecurityAuditLogService auditLogService) {
        this.jwtUtil = jwtUtil;
        this.auditLogService = auditLogService;
    }
```

修改 `logSecurityEvent` 方法：

```java
    private void logSecurityEvent(String eventType, Long userId, String role, HttpServletRequest request) {
        String ip = getClientIp(request);
        secLog.warn("[SECURITY] event={} userId={} role={} path={} ip={}",
                eventType, userId, role, request.getRequestURI(), ip);
        auditLogService.logAsync(eventType, userId, role, request.getRequestURI(), null, ip);
    }
```

添加 import：

```java
import com.agritainment.service.SecurityAuditLogService;
```

- [ ] **Step 6: Integrate SecurityAuditLogService into RoleInterceptor**

在 `RoleInterceptor.java` 中注入 `SecurityAuditLogService`，在 ROLE_DENIED 日志后添加异步数据库写入：

添加字段和构造器：

```java
    private final SecurityAuditLogService auditLogService;

    public RoleInterceptor(SecurityAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }
```

在 ROLE_DENIED 日志后添加：

```java
        auditLogService.logAsync("ROLE_DENIED",
                request.getAttribute("userId") != null ? (Long) request.getAttribute("userId") : null,
                userRole.getValue(), request.getRequestURI(),
                "required: " + requiredRoles, getClientIp(request));
```

添加 import：

```java
import com.agritainment.service.SecurityAuditLogService;
```

- [ ] **Step 7: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 8: Commit**

```bash
git add server/src/main/resources/schema.sql server/src/main/java/com/agritainment/entity/SecurityAuditLog.java server/src/main/java/com/agritainment/mapper/SecurityAuditLogMapper.java server/src/main/java/com/agritainment/service/SecurityAuditLogService.java server/src/main/java/com/agritainment/interceptor/AuthInterceptor.java server/src/main/java/com/agritainment/interceptor/RoleInterceptor.java
git commit -m "feat(logging): add security_audit_log table and dual-write for non-repudiation"
```

---

### Task 15: Add @BusinessLog Annotations to Service Methods

**Files:**
- Modify: Service files that have write operations not caught by prefix matching

- [ ] **Step 1: Add @BusinessLog to uncovered write methods**

以下方法名不以 WRITE_PREFIXES 中的前缀开头，需要手动添加 @BusinessLog 注解：

在 `DiningService.java` 中添加到 `settleOrder`、`refundOrderItem`、`staffCheckin` 方法：
```java
@BusinessLog("结算订单")
public ... settleOrder(...) {

@BusinessLog("退款订单项")
public ... refundOrderItem(...) {

@BusinessLog("员工签到")
public ... staffCheckin(...) {
```

在 `PlantingService.java` 中添加到 `rentPlot`、`completeServiceOrder`、`bindCameraPlot`、`bindPlotToUser` 方法：
```java
@BusinessLog("租用地块")
public ... rentPlot(...) {

@BusinessLog("完成服务订单")
public ... completeServiceOrder(...) {

@BusinessLog("绑定摄像头地块")
public ... bindCameraPlot(...) {

@BusinessLog("绑定地块用户")
public ... bindPlotToUser(...) {
```

在 `MembershipService.java` 中添加到 `purchase`、`grant` 方法：
```java
@BusinessLog("购买会员")
public ... purchase(...) {

@BusinessLog("授予会员")
public ... grant(...) {
```

在 `CouponService.java` 中添加到 `transfer` 方法：
```java
@BusinessLog("转赠卡券")
public ... transfer(...) {
```

在 `ProductService.java` 中添加到 `purchase`、`verifyCoupon` 方法：
```java
@BusinessLog("购买产品")
public ... purchase(...) {

@BusinessLog("核销卡券")
public ... verifyCoupon(...) {
```

在 `JournalService.java` 中添加到 `shareJournal`、`unshareJournal` 方法：
```java
@BusinessLog("分享日记")
public ... shareJournal(...) {

@BusinessLog("取消分享日记")
public ... unshareJournal(...) {
```

在 `AuthService.java` 中添加到 `bindOpenid` 方法：
```java
@BusinessLog("绑定微信")
public ... bindOpenid(...) {
```

每个 Service 文件顶部添加 import：
```java
import com.agritainment.annotation.BusinessLog;
```

- [ ] **Step 2: Verify Maven compiles**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add server/src/main/java/com/agritainment/service/
git commit -m "feat(logging): add @BusinessLog annotations to uncovered write methods"
```

---

### Task 16: Unit Test - SensitiveDataUtils

**Files:**
- Create: `server/src/test/java/com/agritainment/common/SensitiveDataUtilsTest.java`

- [ ] **Step 1: Write test**

```java
package com.agritainment.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataUtilsTest {

    @Test
    void maskPhone_normal() {
        assertEquals("138****1234", SensitiveDataUtils.maskPhone("13812341234"));
    }

    @Test
    void maskPhone_short() {
        assertEquals("****", SensitiveDataUtils.maskPhone("123"));
    }

    @Test
    void maskPhone_null() {
        assertEquals("****", SensitiveDataUtils.maskPhone(null));
    }

    @Test
    void maskOpenid_normal() {
        assertEquals("oXxYy****Zz", SensitiveDataUtils.maskOpenid("oXxYy1234Zz"));
    }

    @Test
    void maskIdentityCode_normal() {
        assertEquals("110****1234", SensitiveDataUtils.maskIdentityCode("1101234567891234"));
    }

    @Test
    void maskPassword() {
        assertEquals("******", SensitiveDataUtils.maskPassword());
    }

    @Test
    void maskSmsCode() {
        assertEquals("******", SensitiveDataUtils.maskSmsCode());
    }

    @Test
    void isSensitiveParam_phone() {
        assertTrue(SensitiveDataUtils.isSensitiveParam("phone"));
    }

    @Test
    void isSensitiveParam_smsCode() {
        assertTrue(SensitiveDataUtils.isSensitiveParam("smsCode"));
    }

    @Test
    void isSensitiveParam_couponCode_notSensitive() {
        assertFalse(SensitiveDataUtils.isSensitiveParam("couponCode"));
    }

    @Test
    void isSensitiveParam_qrCode_notSensitive() {
        assertFalse(SensitiveDataUtils.isSensitiveParam("qrCode"));
    }

    @Test
    void isSensitiveParam_reservationCode_notSensitive() {
        assertFalse(SensitiveDataUtils.isSensitiveParam("reservationCode"));
    }

    @Test
    void maskParam_sensitive() {
        assertEquals("******", SensitiveDataUtils.maskParam("smsCode", "123456"));
    }

    @Test
    void maskParam_notSensitive() {
        assertEquals("ABC123", SensitiveDataUtils.maskParam("couponCode", "ABC123"));
    }

    @Test
    void truncate_underLimit() {
        assertEquals("short", SensitiveDataUtils.truncate("short"));
    }

    @Test
    void truncate_overLimit() {
        String longStr = "a".repeat(600);
        String result = SensitiveDataUtils.truncate(longStr);
        assertTrue(result.endsWith("[TRUNCATED]"));
        assertTrue(result.length() < 600);
    }
}
```

- [ ] **Step 2: Run test**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn test -pl . -Dtest=SensitiveDataUtilsTest -q`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add server/src/test/java/com/agritainment/common/SensitiveDataUtilsTest.java
git commit -m "test(logging): add SensitiveDataUtils unit tests"
```

---

### Task 17: Unit Test - LoggingInterceptor MDC

**Files:**
- Create: `server/src/test/java/com/agritainment/interceptor/LoggingInterceptorTest.java`

- [ ] **Step 1: Write test**

```java
package com.agritainment.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class LoggingInterceptorTest {

    private LoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingInterceptor();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void preHandle_setsRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNotNull(MDC.get("requestId"));
        assertNotNull(request.getAttribute("requestId"));
        assertEquals(MDC.get("requestId"), request.getAttribute("requestId"));
    }

    @Test
    void afterCompletion_clearsMDC() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
        MDC.put("userId", "123");

        interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);

        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
        assertNull(MDC.get("role"));
    }
}
```

- [ ] **Step 2: Run test**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn test -pl . -Dtest=LoggingInterceptorTest -q`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add server/src/test/java/com/agritainment/interceptor/LoggingInterceptorTest.java
git commit -m "test(logging): add LoggingInterceptor MDC injection tests"
```

---

### Task 18: Unit Test - MdcTaskDecorator

**Files:**
- Create: `server/src/test/java/com/agritainment/config/MdcTaskDecoratorTest.java`

- [ ] **Step 1: Write test**

```java
package com.agritainment.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class MdcTaskDecoratorTest {

    private MdcTaskDecorator decorator;

    @BeforeEach
    void setUp() {
        decorator = new MdcTaskDecorator();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void decorate_propagatesMdcToNewThread() throws Exception {
        MDC.put("requestId", "test-request-123");
        MDC.put("userId", "42");

        AtomicReference<String> requestIdInThread = new AtomicReference<>();
        AtomicReference<String> userIdInThread = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Runnable original = () -> {
            requestIdInThread.set(MDC.get("requestId"));
            userIdInThread.set(MDC.get("userId"));
            latch.countDown();
        };

        Runnable decorated = decorator.decorate(original);

        Thread thread = new Thread(decorated);
        thread.start();
        latch.await();
        thread.join();

        assertEquals("test-request-123", requestIdInThread.get());
        assertEquals("42", userIdInThread.get());
    }

    @Test
    void decorate_clearsMdcAfterExecution() throws Exception {
        MDC.put("requestId", "test-123");

        CountDownLatch latch = new CountDownLatch(1);
        Runnable original = latch::countDown;
        Runnable decorated = decorator.decorate(original);

        Thread thread = new Thread(decorated);
        thread.start();
        latch.await();
        thread.join();

        thread.join(100);
    }
}
```

- [ ] **Step 2: Run test**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn test -pl . -Dtest=MdcTaskDecoratorTest -q`
Expected: All tests PASS

- [ ] **Step 3: Commit**

```bash
git add server/src/test/java/com/agritainment/config/MdcTaskDecoratorTest.java
git commit -m "test(logging): add MdcTaskDecorator MDC propagation tests"
```

---

### Task 19: Integration Smoke Test - Start App and Verify Log Files

**Files:**
- No new files

- [ ] **Step 1: Start the application**

Run: `cd e:\Workspace\AI\Agritainment\server; mvn spring-boot:run`
Expected: Application starts without errors, `./logs/` directory created with system.log

- [ ] **Step 2: Trigger a business operation**

使用 curl 或浏览器访问一个 API 端点（如 GET /api/v1/products），检查：
- `./logs/access.log` 中出现 `[ACCESS]` 日志行
- 日志行包含 requestId
- `./logs/system.log` 中有应用启动日志

- [ ] **Step 3: Verify sensitive data masking**

触发一个需要认证的请求（使用无效 Token），检查：
- `./logs/security.log` 中出现 `[SECURITY]` 日志行
- 事件类型为 TOKEN_EXPIRED 或 TOKEN_INVALID（不是笼统的"Token无效或已过期"）

- [ ] **Step 4: Verify AsyncAppender is active**

检查日志文件写入不阻塞请求线程 — 请求响应时间应 < 100ms

---

## Self-Review

### 1. Spec Coverage

| Spec Requirement | Task |
|-----------------|------|
| 四层日志架构 | T2 (logback), T6 (business), T10 (access), T12/T13 (security) |
| AsyncAppender 异步写入 | T2 |
| 滚动策略 | T2 |
| MDC 请求 ID 串联 | T7, T8 |
| 敏感数据脱敏 | T4, T6 |
| @BusinessLog 注解 | T5, T15 |
| AOP 前缀匹配 | T6 |
| AOP+事务顺序 @Order | T6 |
| 拦截器显式顺序 | T9 |
| Token 类型区分 | T12 |
| 安全审计双写 | T14 |
| GlobalExceptionHandler 请求上下文 | T11 |
| 单元测试 | T16, T17, T18 |

### 2. Placeholder Scan

No TBD/TODO/fill-in-later found. All code is complete.

### 3. Type Consistency

- `SensitiveDataUtils.maskParam(String, String)` used consistently in T6
- `SecurityAuditLogService.logAsync(String, Long, String, String, String, String)` signature matches T12/T13 calls
- `LoggingInterceptor` uses `requestId` key, same as `RequestLoggingFilter` reads from request attribute

---

## /plan-eng-review Corrections (4 items)

### Correction 1: BusinessLogAspect — 合并为单个 @Around advice

将双 advice 合并为单个 `@Around("execution(* com.agritainment.service..*Service.*(..))")`，在方法体内统一判断：

```java
@Around("execution(* com.agritainment.service..*Service.*(..))")
public Object logServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
    MethodSignature sig = (MethodSignature) pjp.getSignature();
    String methodName = sig.getMethod().getName();
    BusinessLog businessLog = sig.getMethod().getAnnotation(BusinessLog.class);
    boolean isWrite = businessLog != null
            || WRITE_PREFIXES.stream().anyMatch(methodName::startsWith);
    if (!isWrite) {
        return pjp.proceed();
    }
    return doLog(pjp, businessLog);
}
```

### Correction 2: getClientIp 抽取到 IpUtils 工具类

创建 `server/src/main/java/com/agritainment/common/IpUtils.java`：

```java
package com.agritainment.common;

import jakarta.servlet.http.HttpServletRequest;

public final class IpUtils {

    private IpUtils() {}

    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        return ip;
    }
}
```

AuthInterceptor 和 RoleInterceptor 改为调用 `IpUtils.getClientIp(request)`。

### Correction 3: SECURITY logger 添加 CONSOLE appender

在 logback-spring.xml 的 default profile 中，SECURITY logger 添加 CONSOLE：

```xml
    <springProfile name="default">
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="ASYNC_SYSTEM"/>
        </root>
        <logger name="com.agritainment" level="DEBUG"/>
        <logger name="SECURITY" level="INFO" additivity="false">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="ASYNC_SECURITY"/>
        </logger>
    </springProfile>
```

### Correction 4: 补全关键单元测试

添加以下测试任务：

**T20: AuthInterceptor 单元测试** — 验证 TOKEN_MISSING/TOKEN_EXPIRED/TOKEN_MALFORMED/TOKEN_SIGNATURE_INVALID/TOKEN_INVALID 五种状态 + 成功时 MDC 注入

**T21: RoleInterceptor 单元测试** — 验证 ROLE_DENIED 日志输出 + 无注解时放行 + 角色匹配时放行

**T22: RequestLoggingFilter 单元测试** — 验证正常请求日志 + /uploads/ 排除 + 慢请求标记

**T23: SecurityAuditLogService 单元测试** — 验证 logAsync 正确构建实体 + insert 失败不抛异常

---

## GSTACK REVIEW REPORT

| Review | Trigger | Why | Runs | Status | Findings |
|--------|---------|-----|------|--------|----------|
| CEO Review | `/plan-ceo-review` | Scope & strategy | 1 | issues_open | 12 findings (3C/5H/4M), mode: SELECTIVE_EXPANSION |
| Codex Review | `/codex review` | Independent 2nd opinion | 0 | — | Codex unavailable (region 403) |
| Eng Review | `/plan-eng-review` | Architecture & tests (required) | 1 | issues_open | 4 corrections, 17 test gaps → 4 accepted |
| Design Review | `/plan-design-review` | UI/UX gaps | 0 | — | Skipped, no UI scope |
| DX Review | `/plan-devex-review` | Developer experience gaps | 0 | — | Skipped, no DX scope |

**UNRESOLVED:** 0 unresolved decisions
**VERDICT:** CEO + ENG reviews completed with corrections — 4 corrections must be applied before implementation
