package com.example.admin.dto.activity;

import lombok.EqualsAndHashCode;
import lombok.Data;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActivityDetailDto extends ActivityDto {
    private String content;
}
