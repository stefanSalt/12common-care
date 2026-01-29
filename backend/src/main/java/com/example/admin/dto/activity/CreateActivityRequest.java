package com.example.admin.dto.activity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CreateActivityRequest {
    @NotBlank(message = "title不能为空")
    private String title;

    @NotNull(message = "coverFileId不能为空")
    private Long coverFileId;

    /** Rich text content stored as HTML. */
    @NotBlank(message = "content不能为空")
    private String content;

    private String address;

    @NotNull(message = "startTime不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "endTime不能为空")
    private LocalDateTime endTime;

    /** 0-disabled 1-enabled */
    private Integer signupEnabled;

    /** 0-disabled 1-enabled */
    private Integer donateEnabled;

    private Integer maxParticipants;

    private BigDecimal donationTarget;

    /** 0-disabled 1-enabled */
    private Integer enabled;
}

