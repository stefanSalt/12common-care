package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("biz_story")
public class BizStory {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    /** sys_file.id (PUBLIC) */
    private Long coverFileId;

    /** Rich text content stored as HTML. */
    private String content;

    /** 0-disabled 1-enabled */
    private Integer enabled;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

