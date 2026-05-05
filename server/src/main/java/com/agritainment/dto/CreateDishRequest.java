package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateDishRequest {
    @NotBlank(message = "菜品名称不能为空")
    private String name;
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    private String image_url;
    private String description;
    private Integer remaining_stock;
}
