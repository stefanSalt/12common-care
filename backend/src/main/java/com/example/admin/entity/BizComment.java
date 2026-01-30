package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("biz_comment")
public class BizComment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** STORY */
    private String targetType;

    private Long targetId;

    private Long userId;

    private String content;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;
}

