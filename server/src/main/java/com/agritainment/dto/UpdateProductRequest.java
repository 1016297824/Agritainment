package com.agritainment.dto;

import lombok.Data;

@Data
public class UpdateProductRequest {
    private String name;
    private String type;
    private Double price;
    private Double member_price;
    private String image_url;
    private String description;
    private Integer daily_quota;
    private Boolean is_available;
}
