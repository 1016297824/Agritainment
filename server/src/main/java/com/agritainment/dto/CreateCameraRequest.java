package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCameraRequest {
    @NotBlank(message = "摄像头标识不能为空")
    private String identifier;
    private String name;
    private String ip_address;
}
