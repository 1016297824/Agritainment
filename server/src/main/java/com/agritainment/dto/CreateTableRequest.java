package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTableRequest {
    @NotBlank(message = "桌号不能为空")
    private String table_number;
    private Integer capacity;
}
