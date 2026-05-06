package com.agritainment.service;

import com.agritainment.entity.MembershipConfig;
import com.agritainment.mapper.CouponMapper;
import com.agritainment.mapper.MembershipConfigMapper;
import com.agritainment.mapper.UserMapper;
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
@DisplayName("MembershipService - 会员配置管理")
class MembershipServiceConfigTest {

    @Mock private UserMapper userMapper;
    @Mock private MembershipConfigMapper membershipConfigMapper;
    @Mock private CouponMapper couponMapper;
    @Mock private UserService userService;

    @InjectMocks
    private MembershipService membershipService;

    private MembershipConfig existingConfig;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, MembershipConfig.class);
    }

    @BeforeEach
    void setUp() {
        existingConfig = new MembershipConfig();
        existingConfig.setId(1L);
        existingConfig.setAnnualPrice(199.0);
        existingConfig.setDiscountRate(0.85);
        existingConfig.setGiftProductIds("[1, 2]");
    }

    @Nested
    @DisplayName("updateConfig - 修改会员配置")
    class UpdateConfig {

        @Test
        @DisplayName("更新年费价格：成功")
        void updateAnnualPrice_succeeds() {
            when(membershipConfigMapper.selectOne(any())).thenReturn(existingConfig);
            when(membershipConfigMapper.update(any(), any())).thenReturn(1);
            when(membershipConfigMapper.selectById(1L)).thenReturn(existingConfig);

            MembershipConfig result = membershipService.updateConfig(299.0, null, null);

            assertThat(result).isNotNull();
            verify(membershipConfigMapper).update(any(), any());
        }

        @Test
        @DisplayName("更新折扣率：成功")
        void updateDiscountRate_succeeds() {
            when(membershipConfigMapper.selectOne(any())).thenReturn(existingConfig);
            when(membershipConfigMapper.update(any(), any())).thenReturn(1);
            when(membershipConfigMapper.selectById(1L)).thenReturn(existingConfig);

            MembershipConfig result = membershipService.updateConfig(null, 0.80, null);

            assertThat(result).isNotNull();
            verify(membershipConfigMapper).update(any(), any());
        }

        @Test
        @DisplayName("更新赠送产品：成功")
        void updateGiftProductIds_succeeds() {
            when(membershipConfigMapper.selectOne(any())).thenReturn(existingConfig);
            when(membershipConfigMapper.update(any(), any())).thenReturn(1);
            when(membershipConfigMapper.selectById(1L)).thenReturn(existingConfig);

            MembershipConfig result = membershipService.updateConfig(null, null, "[1, 2, 3]");

            assertThat(result).isNotNull();
            verify(membershipConfigMapper).update(any(), any());
        }

        @Test
        @DisplayName("无现有配置时：创建新配置")
        void noExistingConfig_createsNew() {
            when(membershipConfigMapper.selectOne(any())).thenReturn(null);
            when(membershipConfigMapper.insert(any(MembershipConfig.class))).thenReturn(1);

            MembershipConfig result = membershipService.updateConfig(199.0, 0.85, "[1]");

            assertThat(result).isNotNull();
            verify(membershipConfigMapper).insert(any(MembershipConfig.class));
            verify(membershipConfigMapper, never()).update(any(), any());
        }
    }
}
