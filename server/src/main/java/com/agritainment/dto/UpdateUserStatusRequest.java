package com.agritainment.dto;

import lombok.Data;

@Data
public class UpdateUserStatusRequest {
    private Boolean is_blacklisted;
}
