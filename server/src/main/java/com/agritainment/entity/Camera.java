package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("cameras")
public class Camera {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String identifier;
    private String name;
    private String ipAddress;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
