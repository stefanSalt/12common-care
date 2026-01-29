package com.example.admin.dto.stats;

import lombok.Data;

@Data
public class ActivitySignupRatioDto {
    private Long activityId;

    private String activityTitle;

    private long signupCount;
}

