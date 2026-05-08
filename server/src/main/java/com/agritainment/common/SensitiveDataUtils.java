package com.agritainment.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SensitiveDataUtils {

    private static final int MAX_PARAM_LENGTH = 500;

    private static final Map<String, Supplier<String>> SENSITIVE_PARAM_MASKS = new LinkedHashMap<>();

    static {
        SENSITIVE_PARAM_MASKS.put("phone", () -> "****");
        SENSITIVE_PARAM_MASKS.put("openid", () -> "****");
        SENSITIVE_PARAM_MASKS.put("password", () -> "******");
        SENSITIVE_PARAM_MASKS.put("smsCode", () -> "******");
        SENSITIVE_PARAM_MASKS.put("verifyCode", () -> "******");
        SENSITIVE_PARAM_MASKS.put("identityCode", () -> "****");
    }

    private SensitiveDataUtils() {}

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public static String maskOpenid(String openid) {
        if (openid == null || openid.length() < 8) return "****";
        return openid.substring(0, 5) + "****" + openid.substring(openid.length() - 2);
    }

    public static String maskIdentityCode(String code) {
        if (code == null || code.length() < 4) return "****";
        return code.substring(0, 3) + "****" + code.substring(code.length() - 4);
    }

    public static String maskPassword() {
        return "******";
    }

    public static String maskSmsCode() {
        return "******";
    }

    public static boolean isSensitiveParam(String paramName) {
        return SENSITIVE_PARAM_MASKS.containsKey(paramName);
    }

    public static String maskParam(String paramName, String value) {
        if (value == null) return null;
        Supplier<String> masker = SENSITIVE_PARAM_MASKS.get(paramName);
        if (masker != null) return masker.get();
        return truncate(value);
    }

    public static String truncate(String value) {
        if (value == null) return null;
        if (value.length() <= MAX_PARAM_LENGTH) return value;
        return value.substring(0, MAX_PARAM_LENGTH) + "...[TRUNCATED]";
    }
}
