package com.agritainment.interceptor;

import com.agritainment.annotation.RequireRole;
import com.agritainment.enums.RoleEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleInterceptor - 角色权限检查与安全审计")
class RoleInterceptorTest {

    @InjectMocks
    private RoleInterceptor roleInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @RequireRole(RoleEnum.ADMIN)
    private void adminOnlyMethod() {}

    @RequireRole({RoleEnum.STAFF, RoleEnum.ADMIN})
    private void staffOrAdminMethod() {}

    private void noAnnotationMethod() {}

    @Nested
    @DisplayName("无@RequireRole注解")
    class NoAnnotation {

        @Test
        @DisplayName("无注解方法直接放行")
        void noAnnotation_pass() throws Exception {
            Method method = RoleInterceptorTest.class.getDeclaredMethod("noAnnotationMethod");
            HandlerMethod handlerMethod = new HandlerMethod(new Object(), method);

            boolean result = roleInterceptor.preHandle(request, response, handlerMethod);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("角色匹配")
    class RoleMatch {

        @Test
        @DisplayName("ADMIN角色访问ADMIN方法放行")
        void adminAccessAdminMethod() throws Exception {
            request.setAttribute("role", RoleEnum.ADMIN);
            Method method = RoleInterceptorTest.class.getDeclaredMethod("adminOnlyMethod");
            HandlerMethod handlerMethod = new HandlerMethod(new Object(), method);

            boolean result = roleInterceptor.preHandle(request, response, handlerMethod);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("STAFF角色访问STAFF/ADMIN方法放行")
        void staffAccessStaffOrAdminMethod() throws Exception {
            request.setAttribute("role", RoleEnum.STAFF);
            Method method = RoleInterceptorTest.class.getDeclaredMethod("staffOrAdminMethod");
            HandlerMethod handlerMethod = new HandlerMethod(new Object(), method);

            boolean result = roleInterceptor.preHandle(request, response, handlerMethod);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("ROLE_DENIED")
    class RoleDenied {

        @Test
        @DisplayName("CUSTOMER角色访问ADMIN方法拒绝，返回40301")
        void customerAccessAdminMethod_denied() throws Exception {
            request.setAttribute("userId", 1L);
            request.setAttribute("role", RoleEnum.CUSTOMER);
            Method method = RoleInterceptorTest.class.getDeclaredMethod("adminOnlyMethod");
            HandlerMethod handlerMethod = new HandlerMethod(new Object(), method);

            boolean result = roleInterceptor.preHandle(request, response, handlerMethod);

            assertThat(result).isFalse();
            assertThat(response.getContentAsString()).contains("40301");
        }
    }
}
