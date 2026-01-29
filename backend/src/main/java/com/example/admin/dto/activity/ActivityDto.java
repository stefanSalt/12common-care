package com.example.admin.dto.activity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ActivityDto {
    private Long id;

    private String title;

    private Long coverFileId;

    private String address;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 0-disabled 1-enabled */
    private Integer signupEnabled;

    /** 0-disabled 1-enabled */
    private Integer donateEnabled;

    private Integer maxParticipants;

    private BigDecimal donationTarget;

    private BigDecimal donatedAmount;

    /** 0-disabled 1-enabled */
    private Integer enabled;

    private LocalDateTime createdAt;
}

