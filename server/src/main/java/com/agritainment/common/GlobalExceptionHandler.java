package com.agritainment.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResourceFound(NoResourceFoundException e, HttpServletRequest request) {
        log.debug("[NO_RESOURCE] requestId={} path={} userId={}",
                MDC.get("requestId"), request.getRequestURI(),
                request.getAttribute("userId"));
        return Result.error(404, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        log.error("[SYS_ERROR] requestId={} path={} userId={} error={}",
                MDC.get("requestId"), request.getRequestURI(),
                request.getAttribute("userId"), e.getMessage(), e);
        return Result.error(50000, "服务器内部错误");
    }
}
