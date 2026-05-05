package com.agritainment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateServiceOrderRequest {
    @NotNull(message = "地块ID不能为空")
    private Long plot_id;
    @NotNull(message = "服务ID不能为空")
    private Long service_id;
    private Long coupon_id;
}
