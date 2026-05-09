package com.agritainment.service;

import com.agritainment.annotation.BusinessLog;
import com.agritainment.common.AppException;
import com.agritainment.common.IpUtils;
import com.agritainment.entity.*;
import com.agritainment.enums.RoleEnum;
import com.agritainment.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiningService {

    private final DiningTableMapper tableMapper;
    private final TableReservationMapper reservationMapper;
    private final DishMapper dishMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserService userService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final SecurityAuditLogService securityAuditLogService;
    private final HttpServletRequest request;

    private static final Logger secLog = LoggerFactory.getLogger("SECURITY");

    public List<Map<String, Object>> getTables(LocalDate date, String timeSlot) {
        List<DiningTable> tables = tableMapper.selectList(new LambdaQueryWrapper<DiningTable>().orderByAsc(DiningTable::getTableNumber));
        Set<Long> reservedTableIds = Collections.emptySet();
        if (date != null && timeSlot != null) {
            List<TableReservation> reserved = reservationMapper.selectList(
                    new LambdaQueryWrapper<TableReservation>()
                            .eq(TableReservation::getReservationDate, date)
                            .eq(TableReservation::getTimeSlot, timeSlot)
                            .eq(TableReservation::getStatus, "pending"));
            reservedTableIds = reserved.stream().map(TableReservation::getTableId).collect(Collectors.toSet());
        }
        Set<Long> finalReservedTableIds = reservedTableIds;
        return tables.stream().map(t -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("tableNumber", t.getTableNumber());
            map.put("capacity", t.getCapacity());
            map.put("status", t.getStatus());
            map.put("qrCode", t.getQrCode());
            map.put("reserved", finalReservedTableIds.contains(t.getId()));
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public TableReservation createReservation(Long userId, Long tableId, LocalDate date, String timeSlot) {
        User dbUser = userMapper.selectById(userId);
        if (dbUser == null) throw new AppException(40403, "用户不存在");
        if (Boolean.TRUE.equals(dbUser.getIsBlacklisted())) {
            secLog.warn("[BLACKLIST_BLOCKED] userId={} phone={} path=/api/v1/dining/reservations detail=黑名单用户尝试预约",
                    userId, dbUser.getPhone());
            securityAuditLogService.logAsync("BLACKLIST_BLOCKED", userId, null,
                    "/api/v1/dining/reservations", "黑名单用户尝试预约，phone=" + dbUser.getPhone(),
                    IpUtils.getClientIp(request));
            throw new AppException(40001, "您已被加入黑名单，无法预约");
        }

        Long existing = reservationMapper.selectCount(new LambdaQueryWrapper<TableReservation>()
                .eq(TableReservation::getUserId, userId).eq(TableReservation::getReservationDate, date)
                .eq(TableReservation::getTimeSlot, timeSlot).eq(TableReservation::getStatus, "pending"));
        if (existing > 0) throw new AppException(40002, "该时段已有预约");

        Long reserved = reservationMapper.selectCount(new LambdaQueryWrapper<TableReservation>()
                .eq(TableReservation::getTableId, tableId).eq(TableReservation::getReservationDate, date)
                .eq(TableReservation::getTimeSlot, timeSlot).eq(TableReservation::getStatus, "pending"));
        if (reserved > 0) throw new AppException(40003, "该桌位已被预约");

        TableReservation reservation = new TableReservation();
        reservation.setUserId(userId);
        reservation.setTableId(tableId);
        reservation.setReservationDate(date);
        reservation.setTimeSlot(timeSlot);
        reservation.setStatus("pending");
        reservationMapper.insert(reservation);

        tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, tableId).set(DiningTable::getStatus, "reserved"));

        DiningTable table = tableMapper.selectById(tableId);
        notificationService.notifyReservationCreated(userId, table != null ? table.getTableNumber() : "", date, timeSlot);
        return reservation;
    }

    @Transactional
    public TableReservation cancelReservation(Long userId, Long reservationId) {
        TableReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new AppException(40004, "预约不存在");
        if (!"pending".equals(reservation.getStatus())) throw new AppException(40005, "预约状态不可取消");
        if (!reservation.getUserId().equals(userId)) throw new AppException(40403, "无权取消此预约");

        LocalTime slotTime = "lunch".equals(reservation.getTimeSlot()) ? LocalTime.of(11, 0) : LocalTime.of(17, 0);
        LocalDateTime reservationTime = LocalDateTime.of(reservation.getReservationDate(), slotTime);
        boolean isLateCancel = userService.checkNoShow(userId, reservationTime);

        reservation.setStatus("cancelled");
        reservation.setCancelledBy(RoleEnum.CUSTOMER.getValue());
        reservation.setIsLateCancel(isLateCancel);
        reservationMapper.updateById(reservation);

        DiningTable table = tableMapper.selectById(reservation.getTableId());
        tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, reservation.getTableId()).set(DiningTable::getStatus, "idle"));
        notificationService.notifyReservationCancelled(userId, table != null ? table.getTableNumber() : "", reservation.getReservationDate(), reservation.getTimeSlot());
        return reservation;
    }

    public List<TableReservation> getReservations(Long userId) {
        return reservationMapper.selectList(new LambdaQueryWrapper<TableReservation>()
                .eq(TableReservation::getUserId, userId).orderByDesc(TableReservation::getReservationDate));
    }

    public Order getActiveOrder(String tableQr) {
        DiningTable table = tableMapper.selectOne(new LambdaQueryWrapper<DiningTable>().eq(DiningTable::getQrCode, tableQr));
        if (table == null) throw new AppException(40006, "桌位不存在");
        return orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getTableId, table.getId()).eq(Order::getStatus, "active"));
    }

    public List<Order> getActiveOrders() {
        return orderMapper.selectList(new LambdaQueryWrapper<Order>().eq(Order::getStatus, "active"));
    }

    public List<Dish> getDishes() {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>().eq(Dish::getIsAvailable, true).orderByAsc(Dish::getId));
    }

    @Transactional
    public Order createOrder(Long userId, String tableQr, List<OrderItem> items) {
        DiningTable table = tableMapper.selectOne(new LambdaQueryWrapper<DiningTable>().eq(DiningTable::getQrCode, tableQr));
        if (table == null) throw new AppException(40006, "桌位不存在");

        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getTableId, table.getId()).eq(Order::getStatus, "active"));
        if (order == null) {
            order = new Order();
            order.setTableId(table.getId());
            order.setUserId(userId);
            order.setTotalAmount(BigDecimal.ZERO);
            order.setStatus("active");
            orderMapper.insert(order);
            tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, table.getId()).set(DiningTable::getStatus, "dining"));
        }

        List<Long> dishIds = items.stream().map(OrderItem::getDishId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Dish> dishMap = dishIds.isEmpty() ? Collections.emptyMap()
                : dishMapper.selectBatchIds(dishIds).stream().collect(Collectors.toMap(Dish::getId, d -> d));

        for (OrderItem item : items) {
            Dish dish = dishMap.get(item.getDishId());
            if (dish == null || !dish.getIsAvailable()) continue;
            if (dish.getRemainingStock() != null && dish.getRemainingStock() > 0) {
                if (dish.getRemainingStock() < item.getQuantity()) throw new AppException(40010, "菜品\"" + dish.getName() + "\"库存不足");
                dish.setRemainingStock(dish.getRemainingStock() - item.getQuantity());
                int updated = dishMapper.updateById(dish);
                if (updated == 0) throw new AppException(40010, "菜品\"" + dish.getName() + "\"库存不足，请重试");
            }
            OrderItem oi = new OrderItem();
            oi.setOrderId(order.getId());
            oi.setDishId(item.getDishId());
            oi.setQuantity(item.getQuantity());
            oi.setPrice(dish.getPrice());
            oi.setStatus("pending");
            orderItemMapper.insert(oi);
        }

        recalcOrderTotal(order.getId());
        return orderMapper.selectById(order.getId());
    }

    @Transactional
    @BusinessLog("结算订单")
    public Order settleOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new AppException(40007, "订单不存在");
        order.setStatus("settled");
        order.setSettledAt(LocalDateTime.now());
        orderMapper.updateById(order);
        tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, order.getTableId()).set(DiningTable::getStatus, "idle"));
        return order;
    }

    @Transactional
    public void staffCancelReservation(Long reservationId) {
        TableReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new AppException(40004, "预约不存在");
        LocalTime slotTime = "lunch".equals(reservation.getTimeSlot()) ? LocalTime.of(11, 0) : LocalTime.of(17, 0);
        LocalDateTime reservationTime = LocalDateTime.of(reservation.getReservationDate(), slotTime);
        boolean isLateCancel = userService.checkNoShow(reservation.getUserId(), reservationTime);
        reservation.setStatus("cancelled");
        reservation.setCancelledBy(RoleEnum.STAFF.getValue());
        reservation.setIsLateCancel(isLateCancel);
        reservationMapper.updateById(reservation);
        tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, reservation.getTableId()).set(DiningTable::getStatus, "idle"));
    }

    @BusinessLog("员工签到")
    public void staffCheckin(Long reservationId) {
        TableReservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) throw new AppException(40004, "预约不存在");
        reservation.setStatus("checked_in");
        reservationMapper.updateById(reservation);
        tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, reservation.getTableId()).set(DiningTable::getStatus, "dining"));
    }

    @Transactional
    public void changeTable(Long orderId, Long newTableId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !"active".equals(order.getStatus())) throw new AppException(40007, "订单不存在或已结算");
        DiningTable newTable = tableMapper.selectById(newTableId);
        if (newTable == null) throw new AppException(40006, "目标桌位不存在");

        Long oldTableId = order.getTableId();
        order.setTableId(newTableId);
        orderMapper.updateById(order);

        tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, oldTableId).set(DiningTable::getStatus, "idle"));
        tableMapper.update(null, new LambdaUpdateWrapper<DiningTable>().eq(DiningTable::getId, newTableId).set(DiningTable::getStatus, "dining"));
    }

    @Transactional
    @BusinessLog("退菜退款")
    public void refundOrderItem(Long orderItemId) {
        OrderItem item = orderItemMapper.selectById(orderItemId);
        if (item == null) throw new AppException(40008, "订单项不存在");
        item.setStatus("refunded");
        orderItemMapper.updateById(item);
        recalcOrderTotal(item.getOrderId());
    }

    public DiningTable createTable(String tableNumber, Integer capacity) {
        DiningTable table = new DiningTable();
        table.setTableNumber(tableNumber);
        table.setQrCode("QR_" + tableNumber);
        table.setCapacity(capacity != null ? capacity : 4);
        table.setStatus("idle");
        tableMapper.insert(table);
        return table;
    }

    public void deleteTable(Long id) {
        DiningTable table = tableMapper.selectById(id);
        if (table == null) throw new AppException(40006, "桌位不存在");
        if ("dining".equals(table.getStatus())) throw new AppException(40009, "桌位正在使用，无法删除");
        tableMapper.deleteById(id);
    }

    public List<DiningTable> getAllTables() {
        return tableMapper.selectList(new LambdaQueryWrapper<DiningTable>().orderByAsc(DiningTable::getTableNumber));
    }

    private void recalcOrderTotal(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId).ne(OrderItem::getStatus, "refunded"));
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        orderMapper.update(null, new LambdaUpdateWrapper<Order>().eq(Order::getId, orderId).set(Order::getTotalAmount, total));
    }
}
