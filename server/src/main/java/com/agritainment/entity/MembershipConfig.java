package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("membership_configs")
public class MembershipConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Double annualPrice;
    private Double discountRate;
    private String giftProductIds;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
