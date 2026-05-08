package com.agritainment.service;

import com.agritainment.annotation.BusinessLog;
import com.agritainment.common.AppException;
import com.agritainment.entity.Coupon;
import com.agritainment.entity.ServiceReservation;
import com.agritainment.mapper.CouponMapper;
import com.agritainment.mapper.ProductMapper;
import com.agritainment.mapper.ServiceReservationMapper;
import com.agritainment.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final ServiceReservationMapper serviceReservationMapper;
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    public List<Coupon> getCoupons(Long userId) {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getUserId, userId).orderByDesc(Coupon::getCreatedAt));
    }

    public Coupon getCoupon(Long userId, Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null || !coupon.getUserId().equals(userId))
            throw new AppException(40106, "卡券不存在");
        return coupon;
    }

    @Transactional
    @BusinessLog("转赠优惠券")
    public Coupon transfer(Long userId, Long couponId, Long targetUserId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !coupon.getUserId().equals(userId))
            throw new AppException(40106, "卡券不存在");
        if (!"available".equals(coupon.getStatus()))
            throw new AppException(40107, "卡券已使用，无法转赠");

        String newCode = userService.generateCouponCode();
        coupon.setOriginalUserId(userId);
        coupon.setUserId(targetUserId);
        coupon.setCode(newCode);
        coupon.setQrCodeData("TRANSFER_" + newCode);
        couponMapper.updateById(coupon);
        return coupon;
    }

    @Transactional
    public ServiceReservation createServiceReservation(Long userId, Long couponId, Long productId, java.time.LocalDate date) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || !coupon.getUserId().equals(userId))
            throw new AppException(40106, "卡券不存在");
        if (!"available".equals(coupon.getStatus()))
            throw new AppException(40107, "卡券已使用");

        couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                .eq(Coupon::getId, couponId).set(Coupon::getStatus, "locked"));

        ServiceReservation reservation = new ServiceReservation();
        reservation.setUserId(userId);
        reservation.setCouponId(couponId);
        reservation.setProductId(productId);
        reservation.setReservationDate(date);
        reservation.setStatus("pending");
        reservation.setIsLateCancel(false);
        serviceReservationMapper.insert(reservation);

        String productName = "";
        if (productId != null) {
            var product = productMapper.selectById(productId);
            if (product != null) productName = product.getName();
        }
        notificationService.notifyServiceReservationCreated(userId, productName, date);
        return reservation;
    }

    @Transactional
    public void cancelServiceReservation(Long userId, Long reservationId) {
        ServiceReservation reservation = serviceReservationMapper.selectById(reservationId);
        if (reservation == null) throw new AppException(40108, "服务预约不存在");
        if (!reservation.getUserId().equals(userId)) throw new AppException(40403, "无权取消此预约");
        if (!"pending".equals(reservation.getStatus())) throw new AppException(40109, "预约状态不可取消");

        LocalDateTime reservationTime = reservation.getReservationDate().atTime(LocalTime.of(9, 0));
        boolean isLateCancel = userService.checkNoShow(userId, reservationTime);

        reservation.setStatus("cancelled");
        reservation.setIsLateCancel(isLateCancel);
        serviceReservationMapper.updateById(reservation);

        if (reservation.getCouponId() != null) {
            couponMapper.update(null, new LambdaUpdateWrapper<Coupon>()
                    .eq(Coupon::getId, reservation.getCouponId()).set(Coupon::getStatus, "available"));
        }
    }
}
