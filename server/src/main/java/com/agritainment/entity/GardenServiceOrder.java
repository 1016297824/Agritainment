package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("garden_service_orders")
public class GardenServiceOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long plotId;
    private Long serviceId;
    private Long couponId;
    private String status;
    private Long assignedStaffId;
    private LocalDateTime completedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
