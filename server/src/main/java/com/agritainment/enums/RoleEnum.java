package com.agritainment.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RoleEnum {

    CUSTOMER("customer"),
    STAFF("staff"),
    ADMIN("admin");

    @EnumValue
    @JsonValue
    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static RoleEnum fromValue(String value) {
        for (RoleEnum role : values()) {
            if (role.value.equals(value)) return role;
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
