package com.agritainment.service;

import com.agritainment.entity.Coupon;
import com.agritainment.entity.User;
import com.agritainment.mapper.CouponMapper;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - 爽约黑名单")
class UserServiceNoShowTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<LambdaUpdateWrapper<User>> updateWrapperCaptor;

    private User user;

    @BeforeAll
    static void initMybatisPlusCache() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, User.class);
        TableInfoHelper.initTableInfo(assistant, Coupon.class);
    }

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setNoShowCount(0);
        user.setIsBlacklisted(false);
    }

    @Nested
    @DisplayName("incrementNoShow")
    class IncrementNoShow {

        @Test
        @DisplayName("首次爽约：计数从0→1，不拉黑")
        void firstNoShow_incrementsTo1_notBlacklisted() {
            when(userMapper.selectById(1L)).thenReturn(user);

            userService.incrementNoShow(1L);

            verify(userMapper).update(isNull(), updateWrapperCaptor.capture());
            LambdaUpdateWrapper<User> wrapper = updateWrapperCaptor.getValue();
            assertThat(wrapper.getParamNameValuePairs().get("MPGENVAL1")).isEqualTo(1);
            assertThat(wrapper.getParamNameValuePairs().get("MPGENVAL2")).isEqualTo(false);
        }

        @Test
        @DisplayName("第二次爽约：计数从1→2，不拉黑")
        void secondNoShow_incrementsTo2_notBlacklisted() {
            user.setNoShowCount(1);
            when(userMapper.selectById(1L)).thenReturn(user);

            userService.incrementNoShow(1L);

            verify(userMapper).update(isNull(), updateWrapperCaptor.capture());
            LambdaUpdateWrapper<User> wrapper = updateWrapperCaptor.getValue();
            assertThat(wrapper.getParamNameValuePairs().get("MPGENVAL1")).isEqualTo(2);
            assertThat(wrapper.getParamNameValuePairs().get("MPGENVAL2")).isEqualTo(false);
        }

        @Test
        @DisplayName("第三次爽约：计数从2→3，触发拉黑")
        void thirdNoShow_incrementsTo3_blacklisted() {
            user.setNoShowCount(2);
            when(userMapper.selectById(1L)).thenReturn(user);

            userService.incrementNoShow(1L);

            verify(userMapper).update(isNull(), updateWrapperCaptor.capture());
            LambdaUpdateWrapper<User> wrapper = updateWrapperCaptor.getValue();
            assertThat(wrapper.getParamNameValuePairs().get("MPGENVAL1")).isEqualTo(3);
            assertThat(wrapper.getParamNameValuePairs().get("MPGENVAL2")).isEqualTo(true);
        }

        @Test
        @DisplayName("noShowCount为null时按0处理")
        void nullNoShowCount_treatedAs0() {
            user.setNoShowCount(null);
            when(userMapper.selectById(1L)).thenReturn(user);

            userService.incrementNoShow(1L);

            verify(userMapper).update(isNull(), updateWrapperCaptor.capture());
            LambdaUpdateWrapper<User> wrapper = updateWrapperCaptor.getValue();
            assertThat(wrapper.getParamNameValuePairs().get("MPGENVAL1")).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("checkNoShow")
    class CheckNoShow {

        @Test
        @DisplayName("距预约时间不足6小时：判定爽约，计数+1")
        void lessThan6Hours_isNoShow() {
            java.time.LocalDateTime reservationTime = java.time.LocalDateTime.now().plusHours(5);
            when(userMapper.selectById(1L)).thenReturn(user);

            boolean result = userService.checkNoShow(1L, reservationTime);

            assertThat(result).isTrue();
            verify(userMapper).update(isNull(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("距预约时间超过6小时：不判定爽约")
        void moreThan6Hours_isNotNoShow() {
            java.time.LocalDateTime reservationTime = java.time.LocalDateTime.now().plusHours(7);

            boolean result = userService.checkNoShow(1L, reservationTime);

            assertThat(result).isFalse();
            verify(userMapper, never()).update(any(), any(LambdaUpdateWrapper.class));
        }
    }

    @Nested
    @DisplayName("generateUniqueCode")
    class GenerateUniqueCode {

        @Test
        @DisplayName("首次生成无冲突：直接返回")
        void noCollision_returnsCode() {
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(couponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            String code = userService.generateIdentityCode();

            assertThat(code).hasSize(12).matches("\\d{12}");
        }

        @Test
        @DisplayName("连续3次冲突：抛出异常")
        void threeCollisions_throwsException() {
            when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
            when(couponMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            assertThatThrownBy(() -> userService.generateIdentityCode())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("3 attempts");

            verify(userMapper, times(3)).selectCount(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("第2次成功：返回码")
        void secondAttemptSucceeds() {
            when(userMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(1L).thenReturn(0L);
            when(couponMapper.selectCount(any(LambdaQueryWrapper.class)))
                    .thenReturn(0L).thenReturn(0L);

            String code = userService.generateIdentityCode();

            assertThat(code).hasSize(12).matches("\\d{12}");
            verify(userMapper, times(2)).selectCount(any(LambdaQueryWrapper.class));
        }
    }
}
