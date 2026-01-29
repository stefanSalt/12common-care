package com.example.admin.dto.crowdfunding;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrowdfundingProjectDto {
    private Long id;

    private String title;

    private Long coverFileId;

    private BigDecimal targetAmount;

    private BigDecimal raisedAmount;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** PENDING / APPROVED / REJECTED */
    private String status;

    /** 0-disabled 1-enabled */
    private Integer enabled;

    private Long createdBy;

    private LocalDateTime createdAt;
}

