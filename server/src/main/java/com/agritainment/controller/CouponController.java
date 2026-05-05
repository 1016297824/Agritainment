package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.dto.CreateServiceReservationRequest;
import com.agritainment.dto.TransferCouponRequest;
import com.agritainment.dto.VerifyCouponRequest;
import com.agritainment.entity.Coupon;
import com.agritainment.entity.ServiceReservation;
import com.agritainment.service.CouponService;
import com.agritainment.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final ProductService productService;

    @GetMapping
    @RequireRole({"customer", "staff", "admin"})
    public Result<List<Coupon>> getCoupons(@RequestAttribute("userId") Long userId) {
        return Result.ok(couponService.getCoupons(userId));
    }

    @GetMapping("/{id}")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Coupon> getCoupon(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        return Result.ok(couponService.getCoupon(userId, id));
    }

    @PostMapping("/{id}/transfer")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Coupon> transfer(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TransferCouponRequest request) {
        return Result.ok(couponService.transfer(userId, id, request.getTarget_user_id()));
    }

    @PostMapping("/verify")
    @RequireRole({"staff", "admin"})
    public Result<Coupon> verifyCoupon(@Valid @RequestBody VerifyCouponRequest request) {
        return Result.ok(productService.verifyCoupon(request.getCode()));
    }

    @PostMapping("/service-reservations")
    @RequireRole({"customer", "staff", "admin"})
    public Result<ServiceReservation> createServiceReservation(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateServiceReservationRequest request) {
        return Result.ok(couponService.createServiceReservation(userId, request.getCoupon_id(), request.getProduct_id(), request.getDate()));
    }

    @DeleteMapping("/service-reservations/{id}")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Void> cancelServiceReservation(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        couponService.cancelServiceReservation(userId, id);
        return Result.ok(null);
    }
}
