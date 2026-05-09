package com.agritainment.interceptor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LoggingInterceptor - MDC注入与清理")
class LoggingInterceptorTest {

    private LoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LoggingInterceptor();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("preHandle")
    class PreHandle {

        @Test
        @DisplayName("设置MDC requestId并同步到request attribute")
        void setsRequestId() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();

            boolean result = interceptor.preHandle(request, response, new Object());

            assertThat(result).isTrue();
            assertThat(MDC.get("requestId")).isNotNull();
            assertThat(request.getAttribute("requestId")).isEqualTo(MDC.get("requestId"));
        }

        @Test
        @DisplayName("requestId长度为16")
        void requestIdLength() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();

            interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

            assertThat(MDC.get("requestId")).hasSize(16);
        }
    }

    @Nested
    @DisplayName("afterCompletion")
    class AfterCompletion {

        @Test
        @DisplayName("afterCompletion不主动清除MDC（由RequestLoggingFilter统一管理）")
        void doesNotClearMdc() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            interceptor.preHandle(request, new MockHttpServletResponse(), new Object());
            MDC.put("userId", "123");
            MDC.put("role", "admin");

            interceptor.afterCompletion(request, new MockHttpServletResponse(), new Object(), null);

            assertThat(MDC.get("requestId")).isNotNull();
            assertThat(MDC.get("userId")).isEqualTo("123");
            assertThat(MDC.get("role")).isEqualTo("admin");
        }
    }
}
