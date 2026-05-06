package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.dto.GrantMembershipRequest;
import com.agritainment.entity.MembershipConfig;
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

    @GetMapping("/config")
    public Result<MembershipConfig> getConfig() {
        return Result.ok(membershipService.getConfig());
    }

    @PutMapping("/config")
    @RequireRole({"admin"})
    public Result<MembershipConfig> updateConfig(@RequestBody Map<String, Object> body) {
        Double annualPrice = body.get("annual_price") != null ? Double.valueOf(body.get("annual_price").toString()) : null;
        Double discountRate = body.get("discount_rate") != null ? Double.valueOf(body.get("discount_rate").toString()) : null;
        String giftProductIds = body.get("gift_product_ids") != null ? body.get("gift_product_ids").toString() : null;
        return Result.ok(membershipService.updateConfig(annualPrice, discountRate, giftProductIds));
    }

    @PostMapping("/grant")
    @RequireRole({"admin"})
    public Result<Void> grant(@Valid @RequestBody GrantMembershipRequest request) {
        membershipService.grant(request.getUser_id());
        return Result.ok(null);
    }
}
