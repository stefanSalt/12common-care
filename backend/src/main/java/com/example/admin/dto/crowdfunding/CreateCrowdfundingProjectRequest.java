package com.example.admin.dto.crowdfunding;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateCrowdfundingProjectRequest {
    @NotBlank(message = "title不能为空")
    private String title;

    @NotNull(message = "coverFileId不能为空")
    private Long coverFileId;

    /** Rich text content stored as HTML. */
    @NotBlank(message = "content不能为空")
    private String content;

    @NotNull(message = "targetAmount不能为空")
    @DecimalMin(value = "0.01", message = "targetAmount必须大于0")
    private BigDecimal targetAmount;

    @NotNull(message = "endTime不能为空")
    private LocalDateTime endTime;
}

