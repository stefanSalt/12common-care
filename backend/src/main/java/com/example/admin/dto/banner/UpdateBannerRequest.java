package com.example.admin.dto.banner;

import lombok.Data;

@Data
public class UpdateBannerRequest {
    private String title;

    private Long imageFileId;

    private String linkUrl;

    private Integer sortNo;

    /** 0-disabled 1-enabled */
    private Integer enabled;
}

