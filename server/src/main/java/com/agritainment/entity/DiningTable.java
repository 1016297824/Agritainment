package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("tables")
public class DiningTable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private String qrCode;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
