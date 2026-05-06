package com.agritainment.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WechatService - 微信API封装")
class WechatServiceTest {

    @Spy
    @InjectMocks
    private WechatService wechatService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(wechatService, "appid", "test-appid");
        ReflectionTestUtils.setField(wechatService, "secret", "test-secret");
    }

    @Nested
    @DisplayName("isPlaceholder - 配置占位符检测")
    class IsPlaceholder {

        @Test
        @DisplayName("正常appid：不是占位符")
        void validAppid_notPlaceholder() {
            ReflectionTestUtils.setField(wechatService, "appid", "wx1234567890abcdef");
            assertThat(wechatService.code2Session("test-code")).isNull();
        }

        @Test
        @DisplayName("占位符appid：跳过API调用")
        void placeholderAppid_skipsApi() {
            ReflectionTestUtils.setField(wechatService, "appid", "your-appid-here");
            assertThat(wechatService.code2Session("test-code")).isNull();
        }

        @Test
        @DisplayName("空appid：跳过API调用")
        void emptyAppid_skipsApi() {
            ReflectionTestUtils.setField(wechatService, "appid", "");
            assertThat(wechatService.code2Session("test-code")).isNull();
        }
    }

    @Nested
    @DisplayName("sendSubscribeMessage - 发送订阅消息")
    class SendSubscribeMessage {

        @Test
        @DisplayName("openid为null：不发送")
        void nullOpenid_returnsFalse() {
            boolean result = wechatService.sendSubscribeMessage(null, "tpl123", "pages/test", Map.of());
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("openid为空字符串：不发送")
        void emptyOpenid_returnsFalse() {
            boolean result = wechatService.sendSubscribeMessage("", "tpl123", "pages/test", Map.of());
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("占位符配置：不发送")
        void placeholderConfig_returnsFalse() {
            ReflectionTestUtils.setField(wechatService, "appid", "your-appid-here");
            boolean result = wechatService.sendSubscribeMessage("oTest", "tpl123", "pages/test", Map.of());
            assertThat(result).isFalse();
        }
    }
}
