package com.example.admin.dto.auth;

import com.example.admin.dto.user.UserDto;
import lombok.Data;

@Data
public class LoginResponseData {
    private String token;
    private String refreshToken;
    private UserDto user;
}

