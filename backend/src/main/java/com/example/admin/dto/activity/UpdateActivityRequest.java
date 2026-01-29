package com.example.admin.dto.activity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UpdateActivityRequest {
    private String title;

    private Long coverFileId;

    /** Rich text content stored as HTML. */
    private String content;

    private String address;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 0-disabled 1-enabled */
    private Integer signupEnabled;

    /** 0-disabled 1-enabled */
    private Integer donateEnabled;

    private Integer maxParticipants;

    private BigDecimal donationTarget;

    /** 0-disabled 1-enabled */
    private Integer enabled;
}

