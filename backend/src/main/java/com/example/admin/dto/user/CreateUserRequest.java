package com.example.admin.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;

    /** 0-disabled 1-enabled */
    private Integer status;
}

