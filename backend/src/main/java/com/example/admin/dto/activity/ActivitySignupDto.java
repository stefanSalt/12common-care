package com.example.admin.dto.activity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActivitySignupDto {
    private Long id;

    private Long activityId;

    private Long userId;

    /** SIGNED / CANCELED / CHECKED_IN */
    private String status;

    private LocalDateTime signedAt;

    private LocalDateTime canceledAt;

    private LocalDateTime checkedInAt;

    // denormalized fields (for list pages)
    private String activityTitle;

    private Long activityCoverFileId;

    private LocalDateTime activityStartTime;

    private LocalDateTime activityEndTime;

    private String username;
}

