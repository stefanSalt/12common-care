package com.example.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("biz_crowdfunding_donation")
public class BizCrowdfundingDonation {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long projectId;

    private Long userId;

    private BigDecimal amount;

    /** 0-no 1-yes */
    private Integer isAnonymous;

    private String remark;

    private LocalDateTime createdAt;
}

