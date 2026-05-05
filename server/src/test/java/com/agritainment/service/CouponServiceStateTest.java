package com.agritainment.service;

import com.agritainment.common.AppException;
import com.agritainment.entity.Coupon;
import com.agritainment.entity.ServiceReservation;
import com.agritainment.mapper.CouponMapper;
import com.agritainment.mapper.ServiceReservationMapper;
import com.agritainment.mapper.UserMapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CouponService - 卡券状态流转")
class CouponServiceStateTest {

    @Mock private CouponMapper couponMapper;
    @Mock private ServiceReservationMapper serviceReservationMapper;
    @Mock private UserMapper userMapper;
    @Mock private UserService userService;

    @InjectMocks
    private CouponService couponService;

    private Coupon availableCoupon;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Coupon.class);
        TableInfoHelper.initTableInfo(assistant, ServiceReservation.class);
    }

    @BeforeEach
    void setUp() {
        availableCoupon = new Coupon();
        availableCoupon.setId(1L);
        availableCoupon.setCode("123456789012");
        availableCoupon.setUserId(100L);
        availableCoupon.setStatus("available");
    }

    @Nested
    @DisplayName("transfer - 转赠")
    class Transfer {

        @Test
        @DisplayName("available→转赠成功：更换userId和code")
        void availableCoupon_transferSucceeds() {
            when(couponMapper.selectById(1L)).thenReturn(availableCoupon);
            when(userService.generateCouponCode()).thenReturn("999999999999");
            when(couponMapper.updateById(any(Coupon.class))).thenReturn(1);

            Coupon result = couponService.transfer(100L, 1L, 200L);

            assertThat(result.getUserId()).isEqualTo(200L);
            assertThat(result.getOriginalUserId()).isEqualTo(100L);
            assertThat(result.getCode()).isEqualTo("999999999999");
            verify(couponMapper).updateById(any(Coupon.class));
        }

        @Test
        @DisplayName("非owner转赠：抛出异常")
        void notOwner_throwsException() {
            when(couponMapper.selectById(1L)).thenReturn(availableCoupon);

            assertThatThrownBy(() -> couponService.transfer(999L, 1L, 200L))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40106);
        }

        @Test
        @DisplayName("已使用卡券转赠：抛出异常")
        void usedCoupon_cannotTransfer() {
            availableCoupon.setStatus("used");
            when(couponMapper.selectById(1L)).thenReturn(availableCoupon);

            assertThatThrownBy(() -> couponService.transfer(100L, 1L, 200L))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40107);
        }

        @Test
        @DisplayName("已锁定卡券转赠：抛出异常")
        void lockedCoupon_cannotTransfer() {
            availableCoupon.setStatus("locked");
            when(couponMapper.selectById(1L)).thenReturn(availableCoupon);

            assertThatThrownBy(() -> couponService.transfer(100L, 1L, 200L))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40107);
        }

        @Test
        @DisplayName("卡券不存在：抛出异常")
        void couponNotFound_throwsException() {
            when(couponMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> couponService.transfer(100L, 1L, 200L))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40106);
        }
    }

    @Nested
    @DisplayName("createServiceReservation - 锁定卡券")
    class CreateServiceReservation {

        @Test
        @DisplayName("available→locked：预约成功，卡券状态变为locked")
        void availableCoupon_lockedOnReservation() {
            when(couponMapper.selectById(1L)).thenReturn(availableCoupon);
            when(couponMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(serviceReservationMapper.insert(any(ServiceReservation.class))).thenReturn(1);

            ServiceReservation result = couponService.createServiceReservation(
                    100L, 1L, 50L, LocalDate.now().plusDays(1));

            assertThat(result).isNotNull();
            verify(couponMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("已使用卡券：无法预约")
        void usedCoupon_cannotReserve() {
            availableCoupon.setStatus("used");
            when(couponMapper.selectById(1L)).thenReturn(availableCoupon);

            assertThatThrownBy(() -> couponService.createServiceReservation(
                    100L, 1L, 50L, LocalDate.now().plusDays(1)))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40107);
        }

        @Test
        @DisplayName("已锁定卡券：无法预约")
        void lockedCoupon_cannotReserve() {
            availableCoupon.setStatus("locked");
            when(couponMapper.selectById(1L)).thenReturn(availableCoupon);

            assertThatThrownBy(() -> couponService.createServiceReservation(
                    100L, 1L, 50L, LocalDate.now().plusDays(1)))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40107);
        }
    }

    @Nested
    @DisplayName("cancelServiceReservation - 释放卡券")
    class CancelServiceReservation {

        private ServiceReservation reservation;

        @BeforeEach
        void setUp() {
            reservation = new ServiceReservation();
            reservation.setId(10L);
            reservation.setUserId(100L);
            reservation.setCouponId(1L);
            reservation.setReservationDate(LocalDate.now().plusDays(7));
            reservation.setStatus("pending");
        }

        @Test
        @DisplayName("取消预约：locked→available，卡券释放")
        void cancelReservation_couponReleased() {
            when(serviceReservationMapper.selectById(10L)).thenReturn(reservation);
            when(userService.checkNoShow(eq(100L), any())).thenReturn(false);
            when(serviceReservationMapper.updateById(any(ServiceReservation.class))).thenReturn(1);
            when(couponMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

            couponService.cancelServiceReservation(100L, 10L);

            verify(couponMapper).update(isNull(), any(LambdaUpdateWrapper.class));
            assertThat(reservation.getStatus()).isEqualTo("cancelled");
        }

        @Test
        @DisplayName("非本人取消：抛出异常")
        void notOwner_cannotCancel() {
            when(serviceReservationMapper.selectById(10L)).thenReturn(reservation);

            assertThatThrownBy(() -> couponService.cancelServiceReservation(999L, 10L))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40403);
        }

        @Test
        @DisplayName("非pending状态：无法取消")
        void nonPendingStatus_cannotCancel() {
            reservation.setStatus("completed");
            when(serviceReservationMapper.selectById(10L)).thenReturn(reservation);

            assertThatThrownBy(() -> couponService.cancelServiceReservation(100L, 10L))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40109);
        }

        @Test
        @DisplayName("预约不存在：抛出异常")
        void reservationNotFound_throwsException() {
            when(serviceReservationMapper.selectById(10L)).thenReturn(null);

            assertThatThrownBy(() -> couponService.cancelServiceReservation(100L, 10L))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40108);
        }

        @Test
        @DisplayName("无关联卡券的预约：取消成功但不释放卡券")
        void noCoupon_cancelSucceedsWithoutRelease() {
            reservation.setCouponId(null);
            when(serviceReservationMapper.selectById(10L)).thenReturn(reservation);
            when(userService.checkNoShow(eq(100L), any())).thenReturn(false);
            when(serviceReservationMapper.updateById(any(ServiceReservation.class))).thenReturn(1);

            couponService.cancelServiceReservation(100L, 10L);

            verify(couponMapper, never()).update(any(), any(LambdaUpdateWrapper.class));
            assertThat(reservation.getStatus()).isEqualTo("cancelled");
        }
    }
}
