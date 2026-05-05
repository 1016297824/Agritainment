package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePlotRequest {
    @NotBlank(message = "地块编号不能为空")
    private String plot_number;
    private String name;
    private Double area;
    private String description;
}
