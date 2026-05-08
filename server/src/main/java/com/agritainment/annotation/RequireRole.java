package com.agritainment.annotation;

import com.agritainment.enums.RoleEnum;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    RoleEnum[] value() default {};
}
