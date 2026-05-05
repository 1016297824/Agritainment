package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("dishes")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Double price;
    private String imageUrl;
    private String description;
    private Integer remainingStock;
    @TableField("is_available")
    private Boolean isAvailable;
    @Version
    private Integer version;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
