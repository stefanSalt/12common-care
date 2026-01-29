package com.example.admin.dto.activity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreateActivityDonationRequest {
    @NotNull(message = "amount不能为空")
    @DecimalMin(value = "0.01", message = "amount必须大于0")
    private BigDecimal amount;

    private String remark;
}

