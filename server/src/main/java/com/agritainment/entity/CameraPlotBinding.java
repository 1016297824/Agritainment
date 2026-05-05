package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("camera_plot_bindings")
public class CameraPlotBinding {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cameraId;
    private Long plotId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
