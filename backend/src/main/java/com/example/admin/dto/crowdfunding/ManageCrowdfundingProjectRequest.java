package com.example.admin.dto.crowdfunding;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ManageCrowdfundingProjectRequest {
    private String title;

    private Long coverFileId;

    /** Rich text content stored as HTML. */
    private String content;

    private BigDecimal targetAmount;

    private LocalDateTime endTime;

    /** 0-disabled 1-enabled */
    private Integer enabled;
}

