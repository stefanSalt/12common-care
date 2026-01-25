package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_notification")
public class SysNotification {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    /** SYSTEM / BUSINESS / ANNOUNCEMENT */
    private String type;

    /** 0-unread 1-read */
    private Integer isRead;

    private LocalDateTime createdAt;
}

