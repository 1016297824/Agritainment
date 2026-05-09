package com.agritainment.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.*;

@DisplayName("IpUtils - 客户端IP获取与校验")
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

        @Test
        @DisplayName("X-Forwarded-For 多IP时取第一个")
        void xForwardedForMultiple() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "1.2.3.4, 5.6.7.8, 9.10.11.12");

            assertThat(IpUtils.getClientIp(request)).isEqualTo("1.2.3.4");
        }

        @Test
        @DisplayName("伪造的X-Forwarded-For无效时回退到X-Real-IP")
        void xForwardedForInvalidFallsBack() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "<script>alert(1)</script>");
            request.addHeader("X-Real-IP", "5.6.7.8");

            assertThat(IpUtils.getClientIp(request)).isEqualTo("5.6.7.8");
        }

        @Test
        @DisplayName("所有头都无效时回退到remoteAddr")
        void allHeadersInvalid() {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("X-Forwarded-For", "not-an-ip");
            request.addHeader("X-Real-IP", "also-not-ip");
            request.setRemoteAddr("9.10.11.12");

            assertThat(IpUtils.getClientIp(request)).isEqualTo("9.10.11.12");
        }
    }

    @Nested
    @DisplayName("isValidIp - IP格式校验")
    class IsValidIp {

        @Test
        @DisplayName("合法IPv4地址")
        void validIpv4() {
            assertThat(IpUtils.isValidIp("192.168.1.1")).isTrue();
            assertThat(IpUtils.isValidIp("0.0.0.0")).isTrue();
            assertThat(IpUtils.isValidIp("255.255.255.255")).isTrue();
        }

        @Test
        @DisplayName("非法IPv4地址")
        void invalidIpv4() {
            assertThat(IpUtils.isValidIp("256.1.1.1")).isFalse();
            assertThat(IpUtils.isValidIp("1.2.3")).isFalse();
            assertThat(IpUtils.isValidIp("1.2.3.4.5")).isFalse();
            assertThat(IpUtils.isValidIp("abc.def.ghi.jkl")).isFalse();
        }

        @Test
        @DisplayName("XSS注入攻击无效")
        void xssInjection() {
            assertThat(IpUtils.isValidIp("<script>alert(1)</script>")).isFalse();
            assertThat(IpUtils.isValidIp("1.2.3.4\nX-Header: evil")).isFalse();
        }

        @Test
        @DisplayName("null或空字符串无效")
        void nullOrEmpty() {
            assertThat(IpUtils.isValidIp(null)).isFalse();
            assertThat(IpUtils.isValidIp("")).isFalse();
        }

        @Test
        @DisplayName("超长字符串无效")
        void tooLong() {
            assertThat(IpUtils.isValidIp("1.2.3.4".repeat(20))).isFalse();
        }

        @Test
        @DisplayName("合法IPv6地址")
        void validIpv6() {
            assertThat(IpUtils.isValidIp("::1")).isTrue();
            assertThat(IpUtils.isValidIp("2001:db8::1")).isTrue();
            assertThat(IpUtils.isValidIp("fe80::1%eth0")).isFalse();
        }
    }
}
