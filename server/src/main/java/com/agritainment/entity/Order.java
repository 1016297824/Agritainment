package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tableId;
    private Long userId;
    private Long reservationId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime settledAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
