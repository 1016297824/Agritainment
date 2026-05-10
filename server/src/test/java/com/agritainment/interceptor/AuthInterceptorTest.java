package com.agritainment.interceptor;

import com.agritainment.enums.RoleEnum;
import com.agritainment.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthInterceptor - Token类型区分与安全审计")
class AuthInterceptorTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Nested
    @DisplayName("OPTIONS请求")
    class OptionsRequest {

        @Test
        @DisplayName("OPTIONS请求直接放行")
        void optionsPass() throws Exception {
            request.setMethod("OPTIONS");

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("TOKEN_MISSING")
    class TokenMissing {

        @Test
        @DisplayName("无Authorization头返回40101")
        void noAuthHeader() throws Exception {
            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getContentAsString()).contains("40101");
        }

        @Test
        @DisplayName("非Bearer前缀返回40101")
        void nonBearerHeader() throws Exception {
            request.addHeader("Authorization", "Basic abc123");

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getContentAsString()).contains("40101");
        }
    }

    @Nested
    @DisplayName("TOKEN_EXPIRED")
    class TokenExpired {

        @Test
        @DisplayName("过期Token返回40102")
        void expiredToken() throws Exception {
            request.addHeader("Authorization", "Bearer expired-token");
            when(jwtUtil.parseToken("expired-token")).thenThrow(new ExpiredJwtException(null, null, "expired"));

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getContentAsString()).contains("40102");
        }
    }

    @Nested
    @DisplayName("TOKEN_MALFORMED")
    class TokenMalformed {

        @Test
        @DisplayName("格式错误Token返回40103")
        void malformedToken() throws Exception {
            request.addHeader("Authorization", "Bearer bad-format");
            when(jwtUtil.parseToken("bad-format")).thenThrow(new MalformedJwtException("malformed"));

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getContentAsString()).contains("40103");
        }
    }

    @Nested
    @DisplayName("TOKEN_SIGNATURE_INVALID")
    class TokenSignatureInvalid {

        @Test
        @DisplayName("签名无效Token返回40104")
        void invalidSignature() throws Exception {
            request.addHeader("Authorization", "Bearer tampered-token");
            when(jwtUtil.parseToken("tampered-token")).thenThrow(new SignatureException("bad signature"));

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isFalse();
            assertThat(response.getContentAsString()).contains("40104");
        }
    }

    @Nested
    @DisplayName("认证成功")
    class AuthSuccess {

        @Test
        @DisplayName("有效Token设置userId/role/isMember到request attribute")
        void validToken() throws Exception {
            request.addHeader("Authorization", "Bearer valid-token");
            var claims = mock(io.jsonwebtoken.Claims.class);
            when(claims.getSubject()).thenReturn("42");
            when(claims.get("role", String.class)).thenReturn("customer");
            when(claims.get("isMember", Boolean.class)).thenReturn(true);
            when(jwtUtil.parseToken("valid-token")).thenReturn(claims);

            boolean result = authInterceptor.preHandle(request, response, new Object());

            assertThat(result).isTrue();
            assertThat(request.getAttribute("userId")).isEqualTo(42L);
            assertThat(request.getAttribute("role")).isEqualTo(RoleEnum.CUSTOMER);
            assertThat(request.getAttribute("isMember")).isEqualTo(true);
        }
    }
}
