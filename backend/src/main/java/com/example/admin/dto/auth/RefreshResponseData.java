package com.example.admin.dto.auth;

import lombok.Data;

@Data
public class RefreshResponseData {
    private String token;
    private String refreshToken;
}

