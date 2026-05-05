package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("coupons")
public class Coupon {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private Long productId;
    private Long userId;
    private Long originalUserId;
    private String source;
    private String status;
    private String qrCodeData;
    private LocalDateTime usedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
