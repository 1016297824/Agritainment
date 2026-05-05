package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {
    @NotBlank(message = "产品名称不能为空")
    private String name;
    private String type;
    @NotNull(message = "价格不能为空")
    private Double price;
    private Double member_price;
    private String image_url;
    private String description;
    private Integer daily_quota;
}
