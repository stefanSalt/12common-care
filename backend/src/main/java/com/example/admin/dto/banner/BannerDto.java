package com.example.admin.dto.banner;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BannerDto {
    private Long id;

    private String title;

    private Long imageFileId;

    private String linkUrl;

    private Integer sortNo;

    /** 0-disabled 1-enabled */
    private Integer enabled;

    private LocalDateTime createdAt;
}

