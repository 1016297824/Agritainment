package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("journals")
public class Journal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String images;
    private Boolean isShared;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
