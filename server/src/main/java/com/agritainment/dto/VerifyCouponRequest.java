package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyCouponRequest {
    @NotBlank(message = "卡券码不能为空")
    private String code;
}
