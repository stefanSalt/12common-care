package com.example.admin.dto.crowdfunding;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UpdateCrowdfundingProjectRequest {
    private String title;

    private Long coverFileId;

    /** Rich text content stored as HTML. */
    private String content;

    private BigDecimal targetAmount;

    private LocalDateTime endTime;
}

