package com.agritainment.service;

import com.agritainment.common.AppException;
import com.agritainment.entity.*;
import com.agritainment.mapper.*;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import jakarta.servlet.http.HttpServletRequest;
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

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiningService - 库存竞态")
class DiningServiceStockTest {

    @Mock private DiningTableMapper tableMapper;
    @Mock private TableReservationMapper reservationMapper;
    @Mock private DishMapper dishMapper;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private UserService userService;
    @Mock private UserMapper userMapper;
    @Mock private NotificationService notificationService;
    @Mock private SecurityAuditLogService securityAuditLogService;
    @Mock private HttpServletRequest request;

    @InjectMocks
    private DiningService diningService;

    private DiningTable table;
    private Dish dish;
    private OrderItem orderItem;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, DiningTable.class);
        TableInfoHelper.initTableInfo(assistant, Dish.class);
        TableInfoHelper.initTableInfo(assistant, Order.class);
        TableInfoHelper.initTableInfo(assistant, OrderItem.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
        TableInfoHelper.initTableInfo(assistant, TableReservation.class);
    }

    @BeforeEach
    void setUp() {
        table = new DiningTable();
        table.setId(1L);
        table.setQrCode("QR_A1");
        table.setStatus("idle");

        dish = new Dish();
        dish.setId(10L);
        dish.setName("红烧肉");
        dish.setPrice(new java.math.BigDecimal("38.0"));
        dish.setIsAvailable(true);
        dish.setRemainingStock(5);
        dish.setVersion(0);

        orderItem = new OrderItem();
        orderItem.setDishId(10L);
        orderItem.setQuantity(2);
    }

    @Nested
    @DisplayName("createOrder - 库存扣减")
    class CreateOrderStock {

        @Test
        @DisplayName("库存充足：扣减成功")
        void sufficientStock_deductsSuccessfully() {
            when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(table);
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(100L);
                return 1;
            });
            when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of(dish));
            when(dishMapper.updateById(any(Dish.class))).thenReturn(1);
            when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
            when(orderMapper.selectById(100L)).thenReturn(new Order());

            Order result = diningService.createOrder(1L, "QR_A1", List.of(orderItem));

            assertThat(result).isNotNull();
            verify(dishMapper).updateById(any(Dish.class));
        }

        @Test
        @DisplayName("库存不足：抛出异常，提示库存不足")
        void insufficientStock_throwsException() {
            when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(table);
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(100L);
                return 1;
            });
            when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of(dish));
            when(dishMapper.updateById(any(Dish.class))).thenReturn(0);

            assertThatThrownBy(() -> diningService.createOrder(1L, "QR_A1", List.of(orderItem)))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40010);

            verify(orderItemMapper, never()).insert(any());
        }

        @Test
        @DisplayName("菜品不可用：跳过该菜品，不扣减库存")
        void unavailableDish_skipped() {
            dish.setIsAvailable(false);
            when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(table);
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(100L);
                return 1;
            });
            when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of(dish));
            when(orderMapper.selectById(100L)).thenReturn(new Order());

            Order result = diningService.createOrder(1L, "QR_A1", List.of(orderItem));

            assertThat(result).isNotNull();
            verify(dishMapper, never()).updateById(any(Dish.class));
            verify(orderItemMapper, never()).insert(any());
        }

        @Test
        @DisplayName("库存为null：不限制库存，直接下单")
        void nullStock_noLimit() {
            dish.setRemainingStock(null);
            when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(table);
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(orderMapper.insert(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setId(100L);
                return 1;
            });
            when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of(dish));
            when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
            when(orderMapper.selectById(100L)).thenReturn(new Order());

            Order result = diningService.createOrder(1L, "QR_A1", List.of(orderItem));

            assertThat(result).isNotNull();
            verify(dishMapper, never()).updateById(any(Dish.class));
            verify(orderItemMapper).insert(any(OrderItem.class));
        }

        @Test
        @DisplayName("桌位不存在：抛出异常")
        void tableNotFound_throwsException() {
            when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

            assertThatThrownBy(() -> diningService.createOrder(1L, "QR_INVALID", List.of(orderItem)))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40006);
        }

        @Test
        @DisplayName("已有活跃订单：追加到现有订单")
        void existingActiveOrder_appendsItems() {
            Order existingOrder = new Order();
            existingOrder.setId(100L);
            existingOrder.setTableId(1L);
            existingOrder.setStatus("active");

            when(tableMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(table);
            when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingOrder);
            when(dishMapper.selectBatchIds(anyList())).thenReturn(List.of(dish));
            when(dishMapper.updateById(any(Dish.class))).thenReturn(1);
            when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);
            when(orderMapper.selectById(100L)).thenReturn(existingOrder);

            Order result = diningService.createOrder(1L, "QR_A1", List.of(orderItem));

            assertThat(result).isNotNull();
            verify(orderMapper, never()).insert(any());
            verify(orderItemMapper).insert(any(OrderItem.class));
        }
    }

    @Nested
    @DisplayName("createReservation - 黑名单检查")
    class CreateReservationBlacklist {

        @Test
        @DisplayName("黑名单用户：无法预约")
        void blacklistedUser_cannotReserve() {
            User blacklistedUser = new User();
            blacklistedUser.setId(1L);
            blacklistedUser.setIsBlacklisted(true);
            when(userMapper.selectById(1L)).thenReturn(blacklistedUser);

            assertThatThrownBy(() -> diningService.createReservation(1L, 1L,
                    java.time.LocalDate.now(), "lunch"))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40001);
        }

        @Test
        @DisplayName("用户不存在：抛出异常")
        void userNotFound_throwsException() {
            when(userMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> diningService.createReservation(1L, 1L,
                    java.time.LocalDate.now(), "lunch"))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40403);
        }

        @Test
        @DisplayName("黑名单用户：记录安全审计日志")
        void blacklistedUser_logsSecurityAudit() {
            User blacklistedUser = new User();
            blacklistedUser.setId(1L);
            blacklistedUser.setPhone("13800000000");
            blacklistedUser.setIsBlacklisted(true);
            when(userMapper.selectById(1L)).thenReturn(blacklistedUser);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("192.168.1.100");

            assertThatThrownBy(() -> diningService.createReservation(1L, 1L,
                    java.time.LocalDate.now(), "lunch"))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40001);

            verify(securityAuditLogService).logAsync(
                    eq("BLACKLIST_BLOCKED"), eq(1L), isNull(),
                    eq("/api/v1/dining/reservations"),
                    contains("黑名单用户尝试预约"),
                    eq("192.168.1.100"));
        }
    }
}
