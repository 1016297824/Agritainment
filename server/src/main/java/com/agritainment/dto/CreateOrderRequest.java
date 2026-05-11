package com.agritainment.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private String table_qr;
    private Long reservation_id;
    @NotEmpty(message = "订单项不能为空")
    private List<OrderItemInput> items;

    @Data
    public static class OrderItemInput {
        private Long dish_id;
        private Integer quantity = 1;
    }
}
