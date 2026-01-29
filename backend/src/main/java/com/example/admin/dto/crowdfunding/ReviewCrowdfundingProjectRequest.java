package com.example.admin.dto.crowdfunding;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReviewCrowdfundingProjectRequest {
    /** APPROVE / REJECT */
    @NotBlank(message = "action不能为空")
    private String action;
}

