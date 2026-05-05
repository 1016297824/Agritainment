package com.agritainment.interceptor;

import com.agritainment.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendError(response, 40101, "未登录");
            return false;
        }

        try {
            String token = authHeader.substring(7);
            var claims = jwtUtil.parseToken(token);
            request.setAttribute("userId", Long.parseLong(claims.getSubject()));
            request.setAttribute("role", claims.get("role", String.class));
            request.setAttribute("isMember", claims.get("isMember", Boolean.class));
            return true;
        } catch (Exception e) {
            sendError(response, 40101, "Token无效或已过期");
            return false;
        }
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        new ObjectMapper().writeValue(response.getOutputStream(), Map.of("code", code, "message", message, "data", ""));
    }
}
