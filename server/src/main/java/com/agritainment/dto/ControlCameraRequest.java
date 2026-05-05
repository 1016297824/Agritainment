package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ControlCameraRequest {
    @NotBlank(message = "操作不能为空")
    private String action;
    private Integer speed;
}
