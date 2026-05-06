package com.agritainment.service;

import com.agritainment.common.AppException;
import com.agritainment.entity.Plot;
import com.agritainment.entity.User;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlantingService - 员工绑定地块")
class PlantingServiceBindTest {

    @Mock private PlotMapper plotMapper;
    @Mock private CouponMapper couponMapper;
    @Mock private GardenServiceMapper gardenServiceMapper;
    @Mock private GardenServiceOrderMapper gardenServiceOrderMapper;
    @Mock private CameraMapper cameraMapper;
    @Mock private CameraQueueMapper cameraQueueMapper;
    @Mock private CameraPlotBindingMapper cameraPlotBindingMapper;
    @Mock private UserMapper userMapper;
    @Mock private UserService userService;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private PlantingService plantingService;

    private Plot availablePlot;
    private User customer;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, Plot.class);
        TableInfoHelper.initTableInfo(assistant, User.class);
    }

    @BeforeEach
    void setUp() {
        availablePlot = new Plot();
        availablePlot.setId(1L);
        availablePlot.setPlotNumber("P-001");
        availablePlot.setStatus("available");

        customer = new User();
        customer.setId(100L);
        customer.setIdentityCode("100000000101");
    }

    @Nested
    @DisplayName("bindPlotToUser - 员工通过身份码绑定地块")
    class BindPlotToUser {

        @Test
        @DisplayName("available地块+有效身份码：绑定成功")
        void availablePlot_validIdentityCode_bindSucceeds() {
            when(plotMapper.selectById(1L)).thenReturn(availablePlot);
            when(userMapper.selectOne(any())).thenReturn(customer);
            when(plotMapper.update(any(), any())).thenReturn(1);

            Plot result = plantingService.bindPlotToUser(1L, "100000000101");

            assertThat(result).isNotNull();
            verify(plotMapper).update(any(), any());
        }

        @Test
        @DisplayName("rented地块+有效身份码：重新绑定成功")
        void rentedPlot_validIdentityCode_rebindSucceeds() {
            availablePlot.setStatus("rented");
            availablePlot.setRenterId(99L);
            when(plotMapper.selectById(1L)).thenReturn(availablePlot);
            when(userMapper.selectOne(any())).thenReturn(customer);
            when(plotMapper.update(any(), any())).thenReturn(1);

            Plot result = plantingService.bindPlotToUser(1L, "100000000101");

            assertThat(result).isNotNull();
            verify(plotMapper).update(any(), any());
        }

        @Test
        @DisplayName("maintenance地块：不允许绑定")
        void maintenancePlot_cannotBind() {
            availablePlot.setStatus("maintenance");
            when(plotMapper.selectById(1L)).thenReturn(availablePlot);

            assertThatThrownBy(() -> plantingService.bindPlotToUser(1L, "100000000101"))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40202);
        }

        @Test
        @DisplayName("地块不存在：抛出异常")
        void plotNotFound_throwsException() {
            when(plotMapper.selectById(1L)).thenReturn(null);

            assertThatThrownBy(() -> plantingService.bindPlotToUser(1L, "100000000101"))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40201);
        }

        @Test
        @DisplayName("身份码不存在：抛出异常")
        void identityCodeNotFound_throwsException() {
            when(plotMapper.selectById(1L)).thenReturn(availablePlot);
            when(userMapper.selectOne(any())).thenReturn(null);

            assertThatThrownBy(() -> plantingService.bindPlotToUser(1L, "999999999999"))
                    .isInstanceOf(AppException.class)
                    .extracting("code").isEqualTo(40403);
        }
    }
}
