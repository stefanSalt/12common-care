package com.example.admin.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    /** 0-disabled 1-enabled */
    private Integer status;
}

