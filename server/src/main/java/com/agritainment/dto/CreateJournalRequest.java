package com.agritainment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateJournalRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String images;
}
