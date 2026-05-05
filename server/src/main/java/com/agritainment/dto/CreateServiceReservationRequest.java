package com.agritainment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateServiceReservationRequest {
    @NotNull(message = "卡券ID不能为空")
    private Long coupon_id;
    @NotNull(message = "产品ID不能为空")
    private Long product_id;
    @NotNull(message = "日期不能为空")
    private LocalDate date;
}
