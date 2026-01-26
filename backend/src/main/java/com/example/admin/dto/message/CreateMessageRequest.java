package com.example.admin.dto.message;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateMessageRequest {
    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "留言内容不能为空")
    private String content;

    @NotBlank(message = "联系邮箱不能为空")
    @Email(message = "联系邮箱格式不正确")
    private String contactEmail;
}

