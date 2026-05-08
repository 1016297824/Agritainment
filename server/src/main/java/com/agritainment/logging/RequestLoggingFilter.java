package com.agritainment.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
