package com.example.admin.dto.story;

import lombok.Data;

@Data
public class UpdateStoryRequest {
    private String title;

    private Long coverFileId;

    /** Rich text content stored as HTML. */
    private String content;

    /** 0-disabled 1-enabled */
    private Integer enabled;
}

