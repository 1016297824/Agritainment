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

import java.util.Collection;
import java.util.Map;
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
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

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

    private Object doLog(ProceedingJoinPoint pjp, BusinessLog businessLog) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String className = sig.getDeclaringType().getSimpleName();
        String methodName = sig.getMethod().getName();
        String action = businessLog != null && !businessLog.value().isEmpty()
                ? businessLog.value()
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

        String resultSummary = summarizeResult(result);
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
            if (args[i] == null) {
                sb.append("null");
            } else if (args[i] instanceof String s) {
                sb.append(SensitiveDataUtils.maskParam(paramNames[i], s));
            } else if (args[i] instanceof Number || args[i] instanceof Boolean) {
                sb.append(args[i]);
            } else {
                sb.append(args[i].getClass().getSimpleName())
                        .append("@").append(Integer.toHexString(System.identityHashCode(args[i])));
            }
        }
        sb.append("}");
        return SensitiveDataUtils.truncate(sb.toString());
    }

    private String summarizeResult(Object result) {
        if (result == null) return "void";
        if (result instanceof String s) return SensitiveDataUtils.truncate(s);
        if (result instanceof Number || result instanceof Boolean) return result.toString();
        if (result instanceof Map<?, ?> m) return "Map(size=" + m.size() + ")";
        if (result instanceof Collection<?> c) return result.getClass().getSimpleName() + "(size=" + c.size() + ")";
        return result.getClass().getSimpleName();
    }
}
