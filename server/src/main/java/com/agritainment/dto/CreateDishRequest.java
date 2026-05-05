package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDishRequest {
    @NotBlank(message = "菜品名称不能为空")
    private String name;
    @NotNull(message = "价格不能为空")
    private Double price;
    private String image_url;
    private String description;
    private Integer remaining_stock;
}
