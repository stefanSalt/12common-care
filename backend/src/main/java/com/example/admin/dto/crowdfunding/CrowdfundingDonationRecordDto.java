package com.example.admin.dto.crowdfunding;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrowdfundingDonationRecordDto {
    private Long id;

    private Long projectId;

    private Long userId;

    private BigDecimal amount;

    /** 0-no 1-yes */
    private Integer isAnonymous;

    private String remark;

    private LocalDateTime createdAt;

    // denormalized fields (for list pages)
    private String projectTitle;

    private Long projectCoverFileId;

    private String username;
}

