package com.agritainment.interceptor;

import com.agritainment.common.IpUtils;
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
        String ip = IpUtils.getClientIp(request);
        secLog.warn("[SECURITY] event={} userId={} role={} path={} ip={}",
                eventType, userId, role, request.getRequestURI(), ip);
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        new ObjectMapper().writeValue(response.getOutputStream(), Map.of("code", code, "message", message, "data", ""));
    }
}
