package com.agritainment.service;

import com.agritainment.entity.User;
import com.agritainment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final WechatService wechatService;
    private final UserMapper userMapper;

    @Value("${app.wechat.mini-program.template.reservation-created:}")
    private String reservationCreatedTemplateId;

    @Value("${app.wechat.mini-program.template.reservation-cancelled:}")
    private String reservationCancelledTemplateId;

    @Value("${app.wechat.mini-program.template.service-order-created:}")
    private String serviceOrderCreatedTemplateId;

    @Async
    public void notifyReservationCreated(Long userId, String tableNumber, LocalDate date, String timeSlot) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null) return;

        String timeLabel = "lunch".equals(timeSlot) ? "午餐" : "晚餐";
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", "桌位" + tableNumber + "预约成功"));
        data.put("date2", Map.of("value", date.toString()));
        data.put("thing3", Map.of("value", timeLabel + "时段"));

        wechatService.sendSubscribeMessage(
                user.getOpenid(), reservationCreatedTemplateId,
                "pages/profile/reservations/index", data);
    }

    @Async
    public void notifyReservationCancelled(Long userId, String tableNumber, LocalDate date, String timeSlot) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null) return;

        String timeLabel = "lunch".equals(timeSlot) ? "午餐" : "晚餐";
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", "桌位" + tableNumber + "预约已取消"));
        data.put("date2", Map.of("value", date.toString()));
        data.put("thing3", Map.of("value", timeLabel + "时段"));

        wechatService.sendSubscribeMessage(
                user.getOpenid(), reservationCancelledTemplateId,
                "pages/profile/reservations/index", data);
    }

    @Async
    public void notifyServiceReservationCreated(Long userId, String productName, LocalDate date) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getOpenid() == null) return;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", productName + "服务预约成功"));
        data.put("date2", Map.of("value", date.toString()));
        data.put("thing3", Map.of("value", "请按时到店体验"));

        wechatService.sendSubscribeMessage(
                user.getOpenid(), reservationCreatedTemplateId,
                "pages/profile/reservations/index", data);
    }

    @Async
    public void notifyGardenServiceOrderCreated(Long staffId, String serviceName, String plotNumber) {
        User staff = userMapper.selectById(staffId);
        if (staff == null || staff.getOpenid() == null) return;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("thing1", Map.of("value", "新种植服务订单"));
        data.put("thing2", Map.of("value", serviceName));
        data.put("thing3", Map.of("value", "地块" + plotNumber));

        wechatService.sendSubscribeMessage(
                staff.getOpenid(), serviceOrderCreatedTemplateId,
                "pages/staff/orders/index", data);
    }
}
