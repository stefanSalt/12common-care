package com.example.admin.dto.message;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MessageDto {
    private Long id;
    private Long userId;
    private String username;

    private String title;
    private String content;
    private String contactEmail;

    /** 0-pending 1-replied */
    private Integer status;
    private String replyContent;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;
}

