package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("biz_activity_signup")
public class BizActivitySignup {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long activityId;

    private Long userId;

    /** SIGNED / CANCELED / CHECKED_IN */
    private String status;

    private LocalDateTime signedAt;

    private LocalDateTime canceledAt;

    private LocalDateTime checkedInAt;
}

