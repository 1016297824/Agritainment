package com.agritainment.interceptor;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.IpUtils;
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

    public RoleInterceptor() {
    }

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
                request.getRequestURI(), IpUtils.getClientIp(request));

        sendError(response, 40301, "权限不足");
        return false;
    }

    private void sendError(HttpServletResponse response, int code, String message) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        new ObjectMapper().writeValue(response.getOutputStream(), Map.of("code", code, "message", message, "data", ""));
    }
}
