package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("biz_banner")
public class BizBanner {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    /** sys_file.id (PUBLIC) */
    private Long imageFileId;

    /** Optional link URL when user clicks the banner. */
    private String linkUrl;

    private Integer sortNo;

    /** 0-disabled 1-enabled */
    private Integer enabled;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

