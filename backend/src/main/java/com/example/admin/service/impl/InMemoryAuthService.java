package com.example.admin.service.impl;

import com.example.admin.dto.auth.LoginRequest;
import com.example.admin.dto.auth.LoginResponseData;
import com.example.admin.dto.auth.RefreshRequest;
import com.example.admin.dto.auth.RefreshResponseData;
import com.example.admin.dto.user.UserDto;
import com.example.admin.exception.BusinessException;
import com.example.admin.security.JwtTokenProvider;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class InMemoryAuthService implements AuthService {

    private final InMemoryUserStore userStore;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public InMemoryAuthService(InMemoryUserStore userStore, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userStore = userStore;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponseData login(LoginRequest request) {
        InMemoryUserStore.UserRecord user = userStore.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.passwordHash())) {
            throw new BusinessException(1003, "密码错误");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.id(), user.username(), user.roleCodes());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.id());

        LoginResponseData data = new LoginResponseData();
        data.setToken(accessToken);
        data.setRefreshToken(refreshToken);
        data.setUser(user.toUserDto());
        return data;
    }

    @Override
    public RefreshResponseData refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(refreshToken);
        } catch (Exception e) {
            throw new BadCredentialsException("refresh token invalid", e);
        }
        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new BadCredentialsException("refresh token invalid");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BadCredentialsException("refresh token invalid");
        }

        InMemoryUserStore.UserRecord user = userStore.findById(userId);
        if (user == null) {
            throw new BadCredentialsException("refresh token invalid");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.id(), user.username(), user.roleCodes());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.id());

        RefreshResponseData data = new RefreshResponseData();
        data.setToken(newAccessToken);
        data.setRefreshToken(newRefreshToken);
        return data;
    }

    @Override
    public UserDto me(UserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }

        InMemoryUserStore.UserRecord user = userStore.findById(principal.userId());
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        return user.toUserDto();
    }
}

