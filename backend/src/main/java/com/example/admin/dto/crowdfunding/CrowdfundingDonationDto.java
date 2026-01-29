package com.example.admin.dto.crowdfunding;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CrowdfundingDonationDto {
    private Long id;

    private Long projectId;

    private BigDecimal amount;

    private String donorName;

    private LocalDateTime createdAt;
}

