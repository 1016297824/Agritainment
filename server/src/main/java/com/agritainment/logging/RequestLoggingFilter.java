package com.agritainment.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
            if (requestId != null) {
                MDC.put("requestId", requestId);
            }
            Object userIdAttr = request.getAttribute("userId");
            if (userIdAttr != null) {
                MDC.put("userId", userIdAttr.toString());
            }
            Object roleAttr = request.getAttribute("role");
            if (roleAttr != null) {
                MDC.put("role", roleAttr.toString());
            }

            String userId = userIdAttr != null ? userIdAttr.toString() : "-";
            String method = request.getMethod();
            String uri = request.getRequestURI();
            int status = response.getStatus();
            String slowMark = duration > SLOW_THRESHOLD_MS ? " [SLOW]" : "";
            String logLine = "[ACCESS] requestId={} method={} uri={} status={} duration={}ms userId={}{}";

            if (status >= 500) {
                log.error(logLine, requestId, method, uri, status, duration, userId, slowMark);
            } else if (status >= 400) {
                log.warn(logLine, requestId, method, uri, status, duration, userId, slowMark);
            } else {
                log.info(logLine, requestId, method, uri, status, duration, userId, slowMark);
            }

            MDC.remove("requestId");
            MDC.remove("userId");
            MDC.remove("role");
        }
    }
}
