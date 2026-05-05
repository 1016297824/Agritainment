package com.agritainment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateDishRequest {
    private String name;
    private BigDecimal price;
    private String image_url;
    private String description;
    private Integer remaining_stock;
    private Boolean is_available;
}
