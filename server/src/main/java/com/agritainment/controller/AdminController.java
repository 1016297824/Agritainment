package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.dto.CreateDishRequest;
import com.agritainment.dto.CreatePlotRequest;
import com.agritainment.dto.CreateProductRequest;
import com.agritainment.dto.CreateStaffRequest;
import com.agritainment.dto.UpdateDishRequest;
import com.agritainment.dto.UpdateProductRequest;
import com.agritainment.dto.UpdateUserStatusRequest;
import com.agritainment.entity.*;
import com.agritainment.service.AdminService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @RequireRole({"admin"})
    public Result<Map<String, Object>> getDashboard() {
        return Result.ok(adminService.getDashboard());
    }

    @GetMapping("/users")
    @RequireRole({"admin"})
    public Result<Page<User>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return Result.ok(adminService.getUsers(role, page, pageSize));
    }

    @PutMapping("/users/{id}/status")
    @RequireRole({"admin"})
    public Result<User> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return Result.ok(adminService.updateUserStatus(id, request.getIs_blacklisted()));
    }

    @PostMapping("/staff")
    @RequireRole({"admin"})
    public Result<User> createStaff(@Valid @RequestBody CreateStaffRequest request) {
        return Result.ok(adminService.createStaff(request.getPhone(), request.getNickname()));
    }

    @DeleteMapping("/staff/{id}")
    @RequireRole({"admin"})
    public Result<Void> deleteStaff(@PathVariable Long id) {
        adminService.deleteStaff(id);
        return Result.ok(null);
    }

    @PostMapping("/dishes")
    @RequireRole({"admin"})
    public Result<Dish> createDish(@Valid @RequestBody CreateDishRequest request) {
        return Result.ok(adminService.createDish(
                request.getName(), request.getPrice(), request.getImage_url(),
                request.getDescription(), request.getRemaining_stock()));
    }

    @PutMapping("/dishes/{id}")
    @RequireRole({"admin"})
    public Result<Dish> updateDish(@PathVariable Long id, @Valid @RequestBody UpdateDishRequest request) {
        return Result.ok(adminService.updateDish(id,
                request.getName(), request.getPrice(), request.getImage_url(),
                request.getDescription(), request.getRemaining_stock(), request.getIs_available()));
    }

    @DeleteMapping("/dishes/{id}")
    @RequireRole({"admin"})
    public Result<Void> deleteDish(@PathVariable Long id) {
        adminService.deleteDish(id);
        return Result.ok(null);
    }

    @PostMapping("/products")
    @RequireRole({"admin"})
    public Result<Product> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return Result.ok(adminService.createProduct(
                request.getName(), request.getType(), request.getPrice(),
                request.getMember_price(), request.getImage_url(),
                request.getDescription(), request.getDaily_quota()));
    }

    @PutMapping("/products/{id}")
    @RequireRole({"admin"})
    public Result<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return Result.ok(adminService.updateProduct(id,
                request.getName(), request.getType(), request.getPrice(),
                request.getMember_price(), request.getImage_url(),
                request.getDescription(), request.getDaily_quota(), request.getIs_available()));
    }

    @DeleteMapping("/products/{id}")
    @RequireRole({"admin"})
    public Result<Void> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return Result.ok(null);
    }

    @PostMapping("/plots")
    @RequireRole({"admin"})
    public Result<Plot> createPlot(@Valid @RequestBody CreatePlotRequest request) {
        return Result.ok(adminService.createPlot(
                request.getPlot_number(), request.getName(),
                request.getArea(), request.getDescription()));
    }

    @DeleteMapping("/plots/{id}")
    @RequireRole({"admin"})
    public Result<Void> deletePlot(@PathVariable Long id) {
        adminService.deletePlot(id);
        return Result.ok(null);
    }
}
