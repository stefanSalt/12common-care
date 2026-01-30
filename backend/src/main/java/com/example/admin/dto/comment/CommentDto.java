package com.example.admin.dto.comment;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentDto {
    private Long id;

    private String targetType;

    private Long targetId;

    private Long userId;

    private String content;

    private LocalDateTime createdAt;

    private String username;

    private String userNickname;

    /** Optional: story title when targetType=STORY */
    private String storyTitle;
}

