package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private BigDecimal price;
    private BigDecimal memberPrice;
    private String imageUrl;
    private String description;
    private Integer dailyQuota;
    private Integer remainingQuota;
    @TableField("is_available")
    private Boolean isAvailable;
    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
