package com.example.admin.dto.banner;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBannerRequest {
    private String title;

    @NotNull(message = "imageFileId不能为空")
    private Long imageFileId;

    private String linkUrl;

    private Integer sortNo;

    /** 0-disabled 1-enabled */
    private Integer enabled;
}

