package com.example.admin.dto.activity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActivityDonationDto {
    private Long id;

    private Long activityId;

    private Long userId;

    private BigDecimal amount;

    private String remark;

    private LocalDateTime createdAt;

    // denormalized fields (for list pages)
    private String activityTitle;

    private String username;
}

