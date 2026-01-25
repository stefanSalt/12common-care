package com.example.admin.dto.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnnounceRequest {
    @NotBlank(message = "title不能为空")
    private String title;

    @NotBlank(message = "content不能为空")
    private String content;
}

