package com.example.admin.dto.crowdfunding;

import java.util.List;
import lombok.Data;

@Data
public class CrowdfundingPublicDetailDto {
    private CrowdfundingProjectDetailDto project;

    private List<CrowdfundingDonationDto> latestDonations;
}

