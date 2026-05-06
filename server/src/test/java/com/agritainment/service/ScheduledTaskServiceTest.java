package com.agritainment.service;

import com.agritainment.entity.*;
import com.agritainment.mapper.*;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduledTaskService - 定时任务")
class ScheduledTaskServiceTest {

    @Mock private DishMapper dishMapper;
    @Mock private ProductMapper productMapper;
    @Mock private TableReservationMapper tableReservationMapper;
    @Mock private ServiceReservationMapper serviceReservationMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private ScheduledTaskService scheduledTaskService;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Dish.class);
        TableInfoHelper.initTableInfo(assistant, Product.class);
        TableInfoHelper.initTableInfo(assistant, TableReservation.class);
        TableInfoHelper.initTableInfo(assistant, ServiceReservation.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @Nested
    @DisplayName("resetDailyStock - 每日库存重置")
    class ResetDailyStock {

        @Test
        @DisplayName("有每日库存的菜品：重置remaining_stock为daily_stock")
        void dishWithDailyStock_resetsRemaining() {
            Dish dish = new Dish();
            dish.setId(1L);
            dish.setDailyStock(20);
            dish.setRemainingStock(5);
            when(dishMapper.selectList(null)).thenReturn(List.of(dish));
            when(dishMapper.update(any(), any())).thenReturn(1);
            when(productMapper.selectList(null)).thenReturn(Collections.emptyList());

            scheduledTaskService.resetDailyStock();

            verify(dishMapper).update(any(), any());
        }

        @Test
        @DisplayName("daily_stock=-1的菜品：不重置")
        void dishWithUnlimitedStock_notReset() {
            Dish dish = new Dish();
            dish.setId(1L);
            dish.setDailyStock(-1);
            dish.setRemainingStock(-1);
            when(dishMapper.selectList(null)).thenReturn(List.of(dish));
            when(productMapper.selectList(null)).thenReturn(Collections.emptyList());

            scheduledTaskService.resetDailyStock();

            verify(dishMapper, never()).update(any(), any());
        }

        @Test
        @DisplayName("产品库存重置：remaining_quota重置为daily_quota")
        void productQuota_resetsToDailyQuota() {
            Product product = new Product();
            product.setId(1L);
            product.setDailyQuota(50);
            product.setRemainingQuota(10);
            when(dishMapper.selectList(null)).thenReturn(Collections.emptyList());
            when(productMapper.selectList(null)).thenReturn(List.of(product));
            when(productMapper.update(any(), any())).thenReturn(1);

            scheduledTaskService.resetDailyStock();

            verify(productMapper).update(any(), any());
        }
    }

    @Nested
    @DisplayName("markNoShowReservations - 超时预约处理")
    class MarkNoShowReservations {

        @Test
        @DisplayName("过期桌位预约：标记no_show并增加爽约次数")
        void expiredTableReservation_markedNoShow() {
            TableReservation reservation = new TableReservation();
            reservation.setId(1L);
            reservation.setUserId(100L);
            reservation.setReservationDate(LocalDate.now().minusDays(1));
            reservation.setStatus("pending");

            User user = new User();
            user.setId(100L);
            user.setNoShowCount(0);

            when(tableReservationMapper.selectList(any())).thenReturn(List.of(reservation));
            when(tableReservationMapper.update(any(), any())).thenReturn(1);
            when(userMapper.selectById(100L)).thenReturn(user);
            when(userMapper.update(any(), any())).thenReturn(1);
            when(serviceReservationMapper.selectList(any())).thenReturn(Collections.emptyList());

            scheduledTaskService.markNoShowReservations();

            verify(tableReservationMapper).update(any(), any());
            verify(userMapper).update(any(), any());
        }

        @Test
        @DisplayName("爽约3次：加入黑名单")
        void threeNoShows_blacklisted() {
            TableReservation reservation = new TableReservation();
            reservation.setId(1L);
            reservation.setUserId(100L);
            reservation.setReservationDate(LocalDate.now().minusDays(1));
            reservation.setStatus("pending");

            User user = new User();
            user.setId(100L);
            user.setNoShowCount(2);

            when(tableReservationMapper.selectList(any())).thenReturn(List.of(reservation));
            when(tableReservationMapper.update(any(), any())).thenReturn(1);
            when(userMapper.selectById(100L)).thenReturn(user);
            when(userMapper.update(any(), any())).thenReturn(1);
            when(serviceReservationMapper.selectList(any())).thenReturn(Collections.emptyList());

            scheduledTaskService.markNoShowReservations();

            verify(userMapper).update(any(), any());
        }
    }

    @Nested
    @DisplayName("checkMembershipExpiration - 会员过期检查")
    class CheckMembershipExpiration {

        @Test
        @DisplayName("过期会员：is_member设为false")
        void expiredMember_setToFalse() {
            when(userMapper.update(any(), any())).thenReturn(1);

            scheduledTaskService.checkMembershipExpiration();

            verify(userMapper).update(any(), any());
        }
    }
}
