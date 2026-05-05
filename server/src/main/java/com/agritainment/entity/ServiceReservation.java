package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("service_reservations")
public class ServiceReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    private Long productId;
    private LocalDate reservationDate;
    private String status;
    private Boolean isLateCancel;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
