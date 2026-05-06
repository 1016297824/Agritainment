package com.agritainment.controller;

import com.agritainment.common.Result;
import com.agritainment.dto.AdminLoginRequest;
import com.agritainment.dto.LoginRequest;
import com.agritainment.dto.SmsCodeRequest;
import com.agritainment.dto.WxLoginRequest;
import com.agritainment.entity.User;
import com.agritainment.service.AuthService;
import com.agritainment.service.WechatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final WechatService wechatService;

    @PostMapping("/sms-code")
    public Result<Void> sendSmsCode(@Valid @RequestBody SmsCodeRequest request) {
        authService.sendSmsCode(request.getPhone());
        return Result.ok(null);
    }

    @PostMapping("/register")
    public Result<Map<String, String>> register(@Valid @RequestBody LoginRequest request) {
        String token = authService.register(request.getPhone(), request.getCode());
        return Result.ok(Map.of("token", token));
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getPhone(), request.getCode());
        return Result.ok(Map.of("token", token));
    }

    @PostMapping("/admin-login")
    public Result<Map<String, String>> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        String token = authService.adminLogin(request.getPhone(), request.getPassword());
        return Result.ok(Map.of("token", token));
    }

    @PostMapping("/wx-login")
    public Result<Map<String, String>> wxLogin(@Valid @RequestBody WxLoginRequest request) {
        String openid = wechatService.code2Session(request.getCode());
        if (openid != null) {
            authService.bindOpenid(request.getUserId(), openid);
        }
        return Result.ok(Map.of("openid", openid != null ? openid : ""));
    }

    @GetMapping("/me")
    public Result<User> getMe(@RequestAttribute("userId") Long userId) {
        return Result.ok(authService.getUserInfo(userId));
    }
}
