package com.example.admin.dto.activity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActivityFavoriteDto {
    private Long id;

    private Long activityId;

    private Long userId;

    private LocalDateTime createdAt;

    // denormalized fields (for list pages)
    private String activityTitle;

    private Long activityCoverFileId;

    private String username;
}

