package com.example.admin.dto.crowdfunding;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateCrowdfundingDonationRequest {
    @NotNull(message = "amount不能为空")
    @DecimalMin(value = "0.01", message = "amount必须大于0")
    private BigDecimal amount;

    /** Whether donor name should be hidden in public list. */
    private Boolean anonymous;

    private String remark;
}

