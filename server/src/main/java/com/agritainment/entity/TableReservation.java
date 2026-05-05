package com.agritainment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("table_reservations")
public class TableReservation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long tableId;
    private LocalDate reservationDate;
    private String timeSlot;
    private String status;
    private String cancelledBy;
    private Boolean isLateCancel;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
