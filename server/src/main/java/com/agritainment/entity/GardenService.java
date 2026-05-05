package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("garden_services")
public class GardenService {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    @TableField("is_available")
    private Boolean isAvailable;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
