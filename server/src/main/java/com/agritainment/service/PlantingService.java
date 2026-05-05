package com.agritainment.service;

import com.agritainment.common.AppException;
import com.agritainment.entity.*;
import com.agritainment.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantingService {

    private final PlotMapper plotMapper;
    private final CouponMapper couponMapper;
    private final GardenServiceMapper gardenServiceMapper;
    private final GardenServiceOrderMapper gardenServiceOrderMapper;
    private final CameraMapper cameraMapper;
    private final CameraQueueMapper cameraQueueMapper;
    private final CameraPlotBindingMapper cameraPlotBindingMapper;
    private final UserService userService;

    public List<Plot> getPlots() {
        return plotMapper.selectList(new LambdaQueryWrapper<Plot>().orderByAsc(Plot::getPlotNumber));
    }

    public Plot getPlot(Long id) {
        Plot plot = plotMapper.selectById(id);
        if (plot == null) throw new AppException(40404, "地块不存在");
        return plot;
    }

    @Transactional
    public Plot rentPlot(Long userId, Long plotId) {
        Plot plot = plotMapper.selectById(plotId);
        if (plot == null) throw new AppException(40201, "地块不存在");
        if (!"available".equals(plot.getStatus())) throw new AppException(40202, "地块不可租用");

        String code = userService.generateCouponCode();
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setUserId(userId);
        coupon.setSource("purchase");
        coupon.setStatus("available");
        coupon.setQrCodeData("PLOT_" + code);
        couponMapper.insert(coupon);

        plotMapper.update(null, new LambdaUpdateWrapper<Plot>().eq(Plot::getId, plotId)
                .set(Plot::getRenterId, userId).set(Plot::getStatus, "rented"));
        return plotMapper.selectById(plotId);
    }

    public List<GardenService> getGardenServices() {
        return gardenServiceMapper.selectList(new LambdaQueryWrapper<GardenService>().eq(GardenService::getIsAvailable, true));
    }

    public GardenServiceOrder createServiceOrder(Long userId, Long plotId, Long serviceId, Long couponId) {
        GardenServiceOrder order = new GardenServiceOrder();
        order.setUserId(userId);
        order.setPlotId(plotId);
        order.setServiceId(serviceId);
        order.setCouponId(couponId);
        order.setStatus("pending");
        gardenServiceOrderMapper.insert(order);
        return order;
    }

    public java.util.Map<String, Object> getCameraStatus(Long cameraId) {
        Camera camera = cameraMapper.selectById(cameraId);
        if (camera == null) throw new AppException(40205, "摄像头不存在");
        var queue = cameraQueueMapper.selectList(new LambdaQueryWrapper<CameraQueue>()
                .eq(CameraQueue::getCameraId, cameraId).in(CameraQueue::getStatus, "active", "waiting")
                .orderByAsc(CameraQueue::getQueuePosition));
        return java.util.Map.of("camera", camera, "queue", queue);
    }

    public java.util.Map<String, Object> controlCamera(Long cameraId, String action, Integer speed) {
        Camera camera = cameraMapper.selectById(cameraId);
        if (camera == null) throw new AppException(40205, "摄像头不存在");
        return java.util.Map.of("cameraId", cameraId, "action", action, "status", "command_sent");
    }

    @Transactional
    public GardenServiceOrder completeServiceOrder(Long orderId, Long staffId) {
        GardenServiceOrder order = gardenServiceOrderMapper.selectById(orderId);
        if (order == null) throw new AppException(40206, "服务订单不存在");
        order.setStatus("completed");
        order.setAssignedStaffId(staffId);
        order.setCompletedAt(LocalDateTime.now());
        gardenServiceOrderMapper.updateById(order);
        return order;
    }

    public Camera createCamera(String identifier, String name, String ipAddress) {
        Camera camera = new Camera();
        camera.setIdentifier(identifier);
        camera.setName(name);
        camera.setIpAddress(ipAddress);
        camera.setStatus("offline");
        cameraMapper.insert(camera);
        return camera;
    }

    public void deleteCamera(Long id) {
        cameraMapper.deleteById(id);
    }

    public List<Camera> getCameras() {
        return cameraMapper.selectList(new LambdaQueryWrapper<Camera>().orderByAsc(Camera::getId));
    }

    public void bindCameraPlot(Long cameraId, Long plotId) {
        CameraPlotBinding binding = new CameraPlotBinding();
        binding.setCameraId(cameraId);
        binding.setPlotId(plotId);
        cameraPlotBindingMapper.insert(binding);
    }
}
