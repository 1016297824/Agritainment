package com.agritainment.dto;

import lombok.Data;

@Data
public class UpdateJournalRequest {
    private String title;
    private String content;
    private String images;
}
