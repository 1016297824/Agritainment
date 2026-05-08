package com.agritainment.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IpUtils - 客户端IP获取")
class IpUtilsTest {

    @Nested
    @DisplayName("getClientIp")
    class GetClientIp {

        @Test
        @DisplayName("X-Forwarded-For 存在时优先使用")
        void xForwardedFor() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "1.2.3.4");

            assertThat(IpUtils.getClientIp(request)).isEqualTo("1.2.3.4");
        }

        @Test
        @DisplayName("X-Real-IP 存在时次优先使用")
        void xRealIp() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Real-IP", "5.6.7.8");

            assertThat(IpUtils.getClientIp(request)).isEqualTo("5.6.7.8");
        }

        @Test
        @DisplayName("无代理头时使用 remoteAddr")
        void remoteAddr() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRemoteAddr("9.10.11.12");

            assertThat(IpUtils.getClientIp(request)).isEqualTo("9.10.11.12");
        }

        @Test
        @DisplayName("X-Forwarded-For 优先于 X-Real-IP")
        void xForwardedForOverXRealIp() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "1.2.3.4");
            request.addHeader("X-Real-IP", "5.6.7.8");

            assertThat(IpUtils.getClientIp(request)).isEqualTo("1.2.3.4");
        }
    }
}
