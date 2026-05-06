package com.agritainment.service;

import com.agritainment.entity.*;
import com.agritainment.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskService {

    private final DishMapper dishMapper;
    private final ProductMapper productMapper;
    private final TableReservationMapper tableReservationMapper;
    private final ServiceReservationMapper serviceReservationMapper;
    private final UserMapper userMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void resetDailyStock() {
        log.info("开始每日库存重置");

        List<Dish> dishes = dishMapper.selectList(null);
        for (Dish dish : dishes) {
            if (dish.getDailyStock() != null && dish.getDailyStock() >= 0) {
                dishMapper.update(null, new LambdaUpdateWrapper<Dish>()
                        .eq(Dish::getId, dish.getId())
                        .set(Dish::getRemainingStock, dish.getDailyStock()));
            }
        }

        List<Product> products = productMapper.selectList(null);
        for (Product product : products) {
            if (product.getDailyQuota() != null && product.getDailyQuota() >= 0) {
                productMapper.update(null, new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, product.getId())
                        .set(Product::getRemainingQuota, product.getDailyQuota()));
            }
        }

        log.info("每日库存重置完成: dishes={}, products={}", dishes.size(), products.size());
    }

    @Scheduled(cron = "0 */30 * * * ?")
    @Transactional
    public void markNoShowReservations() {
        log.info("开始处理超时预约");

        LocalDate today = LocalDate.now();

        List<TableReservation> expiredTableReservations = tableReservationMapper.selectList(
                new LambdaQueryWrapper<TableReservation>()
                        .eq(TableReservation::getStatus, "pending")
                        .lt(TableReservation::getReservationDate, today));

        for (TableReservation reservation : expiredTableReservations) {
            tableReservationMapper.update(null, new LambdaUpdateWrapper<TableReservation>()
                    .eq(TableReservation::getId, reservation.getId())
                    .set(TableReservation::getStatus, "no_show"));
            incrementNoShow(reservation.getUserId());
            log.info("桌位预约超时标记no_show: reservationId={}, userId={}", reservation.getId(), reservation.getUserId());
        }

        List<ServiceReservation> expiredServiceReservations = serviceReservationMapper.selectList(
                new LambdaQueryWrapper<ServiceReservation>()
                        .eq(ServiceReservation::getStatus, "pending")
                        .lt(ServiceReservation::getReservationDate, today));

        for (ServiceReservation reservation : expiredServiceReservations) {
            serviceReservationMapper.update(null, new LambdaUpdateWrapper<ServiceReservation>()
                    .eq(ServiceReservation::getId, reservation.getId())
                    .set(ServiceReservation::getStatus, "no_show"));
            incrementNoShow(reservation.getUserId());
            log.info("服务预约超时标记no_show: reservationId={}, userId={}", reservation.getId(), reservation.getUserId());
        }

        log.info("超时预约处理完成: table={}, service={}", expiredTableReservations.size(), expiredServiceReservations.size());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkMembershipExpiration() {
        log.info("开始检查会员过期");

        LocalDate today = LocalDate.now();
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getIsMember, true)
                .le(User::getMemberExpireAt, today)
                .set(User::getIsMember, false));

        log.info("会员过期检查完成");
    }

    private void incrementNoShow(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return;
        int newCount = (user.getNoShowCount() != null ? user.getNoShowCount() : 0) + 1;
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getNoShowCount, newCount)
                .set(User::getIsBlacklisted, newCount >= 3));
    }
}
