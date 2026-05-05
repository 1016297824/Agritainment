package com.agritainment.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateProductRequest {
    private String name;
    private String type;
    private BigDecimal price;
    private BigDecimal member_price;
    private String image_url;
    private String description;
    private Integer daily_quota;
    private Boolean is_available;
}
