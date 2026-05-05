package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateStaffRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;
    private String nickname;
}
