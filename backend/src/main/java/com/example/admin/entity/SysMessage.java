package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("sys_message")
public class SysMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String contactEmail;

    /** 0-pending 1-replied */
    private Integer status;

    private String replyContent;

    private LocalDateTime repliedAt;

    private LocalDateTime createdAt;
}

