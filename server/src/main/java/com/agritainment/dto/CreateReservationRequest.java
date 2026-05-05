package com.agritainment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateReservationRequest {
    @NotNull(message = "桌位ID不能为空")
    private Long table_id;
    @NotNull(message = "日期不能为空")
    private LocalDate date;
    @NotNull(message = "时段不能为空")
    private String time_slot;
}
