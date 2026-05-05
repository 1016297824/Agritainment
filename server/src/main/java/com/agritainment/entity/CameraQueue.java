package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("camera_queue")
public class CameraQueue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cameraId;
    private Long userId;
    private Integer queuePosition;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
