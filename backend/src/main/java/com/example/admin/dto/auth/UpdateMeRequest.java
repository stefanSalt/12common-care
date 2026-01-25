package com.example.admin.dto.auth;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateMeRequest {
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
}

