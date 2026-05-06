package com.agritainment.controller;

import com.agritainment.annotation.RequireRole;
import com.agritainment.common.AppException;
import com.agritainment.common.Result;
import com.agritainment.dto.ControlCameraRequest;
import com.agritainment.dto.CreateCameraRequest;
import com.agritainment.dto.CreateServiceOrderRequest;
import com.agritainment.entity.Camera;
import com.agritainment.entity.GardenService;
import com.agritainment.entity.GardenServiceOrder;
import com.agritainment.entity.Plot;
import com.agritainment.service.PlantingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/planting")
@RequiredArgsConstructor
public class PlantingController {

    private final PlantingService plantingService;

    @GetMapping("/plots")
    public Result<List<Plot>> getPlots() {
        return Result.ok(plantingService.getPlots());
    }

    @GetMapping("/plots/{id}")
    public Result<Plot> getPlot(@PathVariable Long id) {
        return Result.ok(plantingService.getPlot(id));
    }

    @PostMapping("/plots/{id}/rent")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Plot> rentPlot(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        return Result.ok(plantingService.rentPlot(userId, id));
    }

    @GetMapping("/garden-services")
    public Result<List<GardenService>> getGardenServices() {
        return Result.ok(plantingService.getGardenServices());
    }

    @PostMapping("/service-orders")
    @RequireRole({"customer", "staff", "admin"})
    public Result<GardenServiceOrder> createServiceOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateServiceOrderRequest request) {
        return Result.ok(plantingService.createServiceOrder(userId, request.getPlot_id(), request.getService_id(), request.getCoupon_id()));
    }

    @GetMapping("/service-orders")
    @RequireRole({"customer", "staff", "admin"})
    public Result<List<GardenServiceOrder>> getServiceOrders(@RequestAttribute("userId") Long userId) {
        return Result.ok(plantingService.getServiceOrders(userId));
    }

    @GetMapping("/cameras/{id}/status")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Map<String, Object>> getCameraStatus(@PathVariable Long id) {
        return Result.ok(plantingService.getCameraStatus(id));
    }

    @PostMapping("/cameras/{id}/control")
    @RequireRole({"customer", "staff", "admin"})
    public Result<Map<String, Object>> controlCamera(
            @PathVariable Long id,
            @Valid @RequestBody ControlCameraRequest request) {
        return Result.ok(plantingService.controlCamera(id, request.getAction(), request.getSpeed()));
    }

    @PostMapping("/service-orders/{id}/complete")
    @RequireRole({"staff", "admin"})
    public Result<GardenServiceOrder> completeServiceOrder(
            @PathVariable Long id,
            @RequestAttribute("userId") Long staffId) {
        return Result.ok(plantingService.completeServiceOrder(id, staffId));
    }

    @GetMapping("/cameras")
    @RequireRole({"admin"})
    public Result<List<Camera>> getCameras() {
        return Result.ok(plantingService.getCameras());
    }

    @PostMapping("/cameras")
    @RequireRole({"admin"})
    public Result<Camera> createCamera(@Valid @RequestBody CreateCameraRequest request) {
        return Result.ok(plantingService.createCamera(request.getIdentifier(), request.getName(), request.getIp_address()));
    }

    @DeleteMapping("/cameras/{id}")
    @RequireRole({"admin"})
    public Result<Void> deleteCamera(@PathVariable Long id) {
        plantingService.deleteCamera(id);
        return Result.ok(null);
    }

    @PostMapping("/cameras/{cameraId}/bind-plot/{plotId}")
    @RequireRole({"admin"})
    public Result<Void> bindCameraPlot(@PathVariable Long cameraId, @PathVariable Long plotId) {
        plantingService.bindCameraPlot(cameraId, plotId);
        return Result.ok(null);
    }

    @PostMapping("/plots/{id}/bind")
    @RequireRole({"staff", "admin"})
    public Result<Plot> bindPlotToUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String identityCode = body.get("identity_code");
        if (identityCode == null || identityCode.isBlank()) throw new AppException(40001, "identity_code 不能为空");
        return Result.ok(plantingService.bindPlotToUser(id, identityCode));
    }
}
