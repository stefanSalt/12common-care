package com.example.admin.dto.story;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class StoryDto {
    private Long id;

    private String title;

    private Long coverFileId;

    /** 0-disabled 1-enabled */
    private Integer enabled;

    private LocalDateTime createdAt;
}

