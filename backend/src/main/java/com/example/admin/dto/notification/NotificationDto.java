package com.example.admin.dto.notification;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationDto {
    private Long id;
    private String title;
    private String content;
    /** SYSTEM / BUSINESS / ANNOUNCEMENT */
    private String type;
    /** 0-unread 1-read */
    private Integer isRead;
    private LocalDateTime createdAt;
}

