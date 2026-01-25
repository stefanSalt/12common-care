package com.example.admin.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}

