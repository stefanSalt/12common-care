package com.example.admin.dto.story;

import lombok.Data;

@Data
public class StoryDetailDto extends StoryDto {
    /** Rich text content stored as HTML. */
    private String content;
}

