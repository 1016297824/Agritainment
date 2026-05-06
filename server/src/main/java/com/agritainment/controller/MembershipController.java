package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.dto.GrantMembershipRequest;
import com.agritainment.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/status")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Map<String, Object>> getStatus(@RequestAttribute("userId") Long userId) {
        return Result.ok(membershipService.getStatus(userId));
    }

    @PostMapping("/purchase")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Map<String, Object>> purchase(@RequestAttribute("userId") Long userId) {
        return Result.ok(membershipService.purchase(userId));
    }

    @PostMapping("/grant")
    @RequireRole({"admin"})
    public Result<Void> grant(@Valid @RequestBody GrantMembershipRequest request) {
        membershipService.grant(request.getUser_id());
        return Result.ok(null);
    }
}
