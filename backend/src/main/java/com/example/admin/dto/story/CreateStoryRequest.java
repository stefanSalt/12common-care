package com.example.admin.dto.story;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateStoryRequest {
    @NotBlank(message = "title不能为空")
    private String title;

    @NotNull(message = "coverFileId不能为空")
    private Long coverFileId;

    /** Rich text content stored as HTML. */
    @NotBlank(message = "content不能为空")
    private String content;

    /** 0-disabled 1-enabled */
    private Integer enabled;
}

