package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("plots")
public class Plot {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String plotNumber;
    private String name;
    private Double area;
    private String description;
    private Long renterId;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
