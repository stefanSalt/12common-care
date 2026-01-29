package com.example.admin.dto.crowdfunding;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CrowdfundingProjectDetailDto extends CrowdfundingProjectDto {
    /** Rich text content stored as HTML. */
    private String content;
}

