package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.Result;
import com.agritainment.dto.ChangeTableRequest;
import com.agritainment.dto.CreateOrderRequest;
import com.agritainment.dto.CreateReservationRequest;
import com.agritainment.dto.CreateTableRequest;
import com.agritainment.entity.*;
import com.agritainment.enums.RoleEnum;
import com.agritainment.service.DiningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dining")
@RequiredArgsConstructor
public class DiningController {

    private final DiningService diningService;

    @GetMapping("/tables")
    public Result<List<Map<String, Object>>> getTables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String time_slot) {
        return Result.ok(diningService.getTables(date, time_slot));
    }

    @PostMapping("/reservations")
    @RequireRole({RoleEnum.CUSTOMER, RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<TableReservation> createReservation(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateReservationRequest request) {
        return Result.ok(diningService.createReservation(userId, request.getTable_id(), request.getDate(), request.getTime_slot()));
    }

    @DeleteMapping("/reservations/{id}")
    @RequireRole({RoleEnum.CUSTOMER, RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<TableReservation> cancelReservation(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        return Result.ok(diningService.cancelReservation(userId, id));
    }

    @GetMapping("/reservations")
    @RequireRole({RoleEnum.CUSTOMER, RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<List<TableReservation>> getReservations(@RequestAttribute("userId") Long userId) {
        return Result.ok(diningService.getReservations(userId));
    }

    @GetMapping("/orders/active")
    @RequireRole({RoleEnum.CUSTOMER, RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Object> getActiveOrder(@RequestParam(required = false) String table_qr) {
        if (table_qr != null) {
            return Result.ok(diningService.getActiveOrder(table_qr));
        }
        return Result.ok(diningService.getActiveOrders());
    }

    @PostMapping("/orders")
    @RequireRole({RoleEnum.CUSTOMER, RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Order> createOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateOrderRequest request) {
        List<OrderItem> items = request.getItems().stream().map(input -> {
            OrderItem oi = new OrderItem();
            oi.setDishId(input.getDish_id());
            oi.setQuantity(input.getQuantity());
            return oi;
        }).toList();
        return Result.ok(diningService.createOrder(userId, request.getTable_qr(), items));
    }

    @PostMapping("/orders/{id}/settle")
    @RequireRole({RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Order> settleOrder(@PathVariable Long id) {
        return Result.ok(diningService.settleOrder(id));
    }

    @GetMapping("/dishes")
    public Result<List<Dish>> getDishes() {
        return Result.ok(diningService.getDishes());
    }

    @PostMapping("/staff/reservations/{id}/checkin")
    @RequireRole({RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Void> staffCheckin(@PathVariable Long id) {
        diningService.staffCheckin(id);
        return Result.ok(null);
    }

    @PostMapping("/staff/reservations/{id}/cancel")
    @RequireRole({RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Void> staffCancel(@PathVariable Long id) {
        diningService.staffCancelReservation(id);
        return Result.ok(null);
    }

    @PostMapping("/orders/{id}/change-table")
    @RequireRole({RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Void> changeTable(@PathVariable Long id, @Valid @RequestBody ChangeTableRequest request) {
        diningService.changeTable(id, request.getNew_table_id());
        return Result.ok(null);
    }

    @PostMapping("/order-items/{id}/refund")
    @RequireRole({RoleEnum.STAFF, RoleEnum.ADMIN})
    public Result<Void> refundOrderItem(@PathVariable Long id) {
        diningService.refundOrderItem(id);
        return Result.ok(null);
    }

    @GetMapping("/all-tables")
    @RequireRole({RoleEnum.ADMIN})
    public Result<List<DiningTable>> getAllTables() {
        return Result.ok(diningService.getAllTables());
    }

    @PostMapping("/tables")
    @RequireRole({RoleEnum.ADMIN})
    public Result<DiningTable> createTable(@Valid @RequestBody CreateTableRequest request) {
        return Result.ok(diningService.createTable(request.getTable_number(), request.getCapacity()));
    }

    @DeleteMapping("/tables/{id}")
    @RequireRole({RoleEnum.ADMIN})
    public Result<Void> deleteTable(@PathVariable Long id) {
        diningService.deleteTable(id);
        return Result.ok(null);
    }
}
