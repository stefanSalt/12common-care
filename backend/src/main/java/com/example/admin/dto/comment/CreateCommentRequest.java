package com.example.admin.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotBlank(message = "targetType不能为空")
    private String targetType;

    @NotNull(message = "targetId不能为空")
    private Long targetId;

    @NotBlank(message = "content不能为空")
    private String content;
}

