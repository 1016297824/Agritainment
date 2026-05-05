package com.agritainment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferCouponRequest {
    @NotNull(message = "目标用户ID不能为空")
    private Long target_user_id;
}
