package com.agritainment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GrantMembershipRequest {
    @NotNull(message = "用户ID不能为空")
    private Long user_id;
}
