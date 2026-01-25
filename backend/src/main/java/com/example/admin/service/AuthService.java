package com.example.admin.service;

import com.example.admin.dto.auth.LoginRequest;
import com.example.admin.dto.auth.LoginResponseData;
import com.example.admin.dto.auth.RefreshRequest;
import com.example.admin.dto.auth.RefreshResponseData;
import com.example.admin.dto.user.UserDto;
import com.example.admin.security.UserPrincipal;

public interface AuthService {
    LoginResponseData login(LoginRequest request);

    RefreshResponseData refresh(RefreshRequest request);

    UserDto me(UserPrincipal principal);
}

