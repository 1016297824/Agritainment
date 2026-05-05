package com.agritainment.dto;

import lombok.Data;

@Data
public class UpdateDishRequest {
    private String name;
    private Double price;
    private String image_url;
    private String description;
    private Integer remaining_stock;
    private Boolean is_available;
}
