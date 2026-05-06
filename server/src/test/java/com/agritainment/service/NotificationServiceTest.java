package com.agritainment.service;

import com.agritainment.entity.User;
import com.agritainment.mapper.UserMapper;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService - 订阅消息通知")
class NotificationServiceTest {

    @Mock private WechatService wechatService;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User userWithOpenid;
    private User userWithoutOpenid;

    @BeforeEach
    void setUp() {
        userWithOpenid = new User();
        userWithOpenid.setId(1L);
        userWithOpenid.setOpenid("oTestOpenid123");

        userWithoutOpenid = new User();
        userWithoutOpenid.setId(2L);
        userWithoutOpenid.setOpenid(null);
    }

    @Nested
    @DisplayName("notifyReservationCreated - 桌位预约成功通知")
    class NotifyReservationCreated {

        @Test
        @DisplayName("有openid的用户：发送通知")
        void userWithOpenid_sendsNotification() {
            when(userMapper.selectById(1L)).thenReturn(userWithOpenid);
            when(wechatService.sendSubscribeMessage(any(), any(), any(), any())).thenReturn(true);

            notificationService.notifyReservationCreated(1L, "A1", LocalDate.now(), "lunch");

            verify(wechatService).sendSubscribeMessage(
                    eq("oTestOpenid123"), any(), eq("pages/profile/reservations/index"), any());
        }

        @Test
        @DisplayName("无openid的用户：不发送通知")
        void userWithoutOpenid_skipsNotification() {
            when(userMapper.selectById(2L)).thenReturn(userWithoutOpenid);

            notificationService.notifyReservationCreated(2L, "A1", LocalDate.now(), "lunch");

            verify(wechatService, never()).sendSubscribeMessage(any(), any(), any(), any());
        }

        @Test
        @DisplayName("用户不存在：不发送通知")
        void userNotFound_skipsNotification() {
            when(userMapper.selectById(999L)).thenReturn(null);

            notificationService.notifyReservationCreated(999L, "A1", LocalDate.now(), "lunch");

            verify(wechatService, never()).sendSubscribeMessage(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("notifyReservationCancelled - 预约取消通知")
    class NotifyReservationCancelled {

        @Test
        @DisplayName("有openid的用户：发送取消通知")
        void userWithOpenid_sendsCancellation() {
            when(userMapper.selectById(1L)).thenReturn(userWithOpenid);
            when(wechatService.sendSubscribeMessage(any(), any(), any(), any())).thenReturn(true);

            notificationService.notifyReservationCancelled(1L, "B2", LocalDate.now(), "dinner");

            verify(wechatService).sendSubscribeMessage(
                    eq("oTestOpenid123"), any(), eq("pages/profile/reservations/index"), any());
        }
    }

    @Nested
    @DisplayName("notifyGardenServiceOrderCreated - 种植服务下单通知")
    class NotifyGardenServiceOrderCreated {

        @Test
        @DisplayName("有openid的员工：发送服务下单通知")
        void staffWithOpenid_sendsNotification() {
            when(userMapper.selectById(1L)).thenReturn(userWithOpenid);
            when(wechatService.sendSubscribeMessage(any(), any(), any(), any())).thenReturn(true);

            notificationService.notifyGardenServiceOrderCreated(1L, "浇水", "P001");

            verify(wechatService).sendSubscribeMessage(
                    eq("oTestOpenid123"), any(), eq("pages/staff/orders/index"), any());
        }
    }
}
