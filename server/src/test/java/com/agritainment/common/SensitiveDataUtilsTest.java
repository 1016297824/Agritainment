package com.agritainment.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SensitiveDataUtils - 敏感数据脱敏")
class SensitiveDataUtilsTest {

    @Nested
    @DisplayName("maskPhone")
    class MaskPhone {

        @Test
        @DisplayName("正常手机号：前3后4中间星号")
        void normalPhone() {
            assertThat(SensitiveDataUtils.maskPhone("13812341234")).isEqualTo("138****1234");
        }

        @Test
        @DisplayName("短号码：返回星号")
        void shortPhone() {
            assertThat(SensitiveDataUtils.maskPhone("123")).isEqualTo("****");
        }

        @Test
        @DisplayName("null：返回星号")
        void nullPhone() {
            assertThat(SensitiveDataUtils.maskPhone(null)).isEqualTo("****");
        }
    }

    @Nested
    @DisplayName("maskOpenid")
    class MaskOpenid {

        @Test
        @DisplayName("正常openid：前5后2中间星号")
        void normalOpenid() {
            assertThat(SensitiveDataUtils.maskOpenid("oXxYy1234Zz")).isEqualTo("oXxYy****Zz");
        }

        @Test
        @DisplayName("短openid：返回星号")
        void shortOpenid() {
            assertThat(SensitiveDataUtils.maskOpenid("abc")).isEqualTo("****");
        }
    }

    @Nested
    @DisplayName("maskIdentityCode")
    class MaskIdentityCode {

        @Test
        @DisplayName("正常身份证：前3后4中间星号")
        void normalCode() {
            assertThat(SensitiveDataUtils.maskIdentityCode("1101234567891234")).isEqualTo("110****1234");
        }

        @Test
        @DisplayName("短码：返回星号")
        void shortCode() {
            assertThat(SensitiveDataUtils.maskIdentityCode("12")).isEqualTo("****");
        }
    }

    @Nested
    @DisplayName("maskPassword / maskSmsCode")
    class MaskFixed {

        @Test
        @DisplayName("密码脱敏为6星号")
        void password() {
            assertThat(SensitiveDataUtils.maskPassword()).isEqualTo("******");
        }

        @Test
        @DisplayName("验证码脱敏为6星号")
        void smsCode() {
            assertThat(SensitiveDataUtils.maskSmsCode()).isEqualTo("******");
        }
    }

    @Nested
    @DisplayName("isSensitiveParam - 精确匹配（Critical Gap #3 修复验证）")
    class IsSensitiveParam {

        @Test
        @DisplayName("phone 是敏感参数")
        void phoneSensitive() {
            assertThat(SensitiveDataUtils.isSensitiveParam("phone")).isTrue();
        }

        @Test
        @DisplayName("password 是敏感参数")
        void passwordSensitive() {
            assertThat(SensitiveDataUtils.isSensitiveParam("password")).isTrue();
        }

        @Test
        @DisplayName("smsCode 是敏感参数")
        void smsCodeSensitive() {
            assertThat(SensitiveDataUtils.isSensitiveParam("smsCode")).isTrue();
        }

        @Test
        @DisplayName("verifyCode 是敏感参数")
        void verifyCodeSensitive() {
            assertThat(SensitiveDataUtils.isSensitiveParam("verifyCode")).isTrue();
        }

        @Test
        @DisplayName("couponCode 不是敏感参数（关键：不误杀）")
        void couponCodeNotSensitive() {
            assertThat(SensitiveDataUtils.isSensitiveParam("couponCode")).isFalse();
        }

        @Test
        @DisplayName("qrCode 不是敏感参数")
        void qrCodeNotSensitive() {
            assertThat(SensitiveDataUtils.isSensitiveParam("qrCode")).isFalse();
        }

        @Test
        @DisplayName("reservationCode 不是敏感参数")
        void reservationCodeNotSensitive() {
            assertThat(SensitiveDataUtils.isSensitiveParam("reservationCode")).isFalse();
        }
    }

    @Nested
    @DisplayName("maskParam")
    class MaskParam {

        @Test
        @DisplayName("敏感参数返回脱敏值")
        void sensitiveParam() {
            assertThat(SensitiveDataUtils.maskParam("smsCode", "123456")).isEqualTo("******");
        }

        @Test
        @DisplayName("非敏感参数返回截断后的原值")
        void notSensitiveParam() {
            assertThat(SensitiveDataUtils.maskParam("couponCode", "ABC123")).isEqualTo("ABC123");
        }

        @Test
        @DisplayName("null值返回null")
        void nullValue() {
            assertThat(SensitiveDataUtils.maskParam("couponCode", null)).isNull();
        }
    }

    @Nested
    @DisplayName("truncate")
    class Truncate {

        @Test
        @DisplayName("短字符串不截断")
        void shortString() {
            assertThat(SensitiveDataUtils.truncate("short")).isEqualTo("short");
        }

        @Test
        @DisplayName("超长字符串截断并标记")
        void longString() {
            String longStr = "a".repeat(600);
            String result = SensitiveDataUtils.truncate(longStr);
            assertThat(result).endsWith("[TRUNCATED]");
            assertThat(result.length()).isLessThan(600);
        }

        @Test
        @DisplayName("null返回null")
        void nullString() {
            assertThat(SensitiveDataUtils.truncate(null)).isNull();
        }
    }
}
