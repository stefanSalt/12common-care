package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.admin.dto.auth.LoginRequest;
import com.example.admin.dto.auth.LoginResponseData;
import com.example.admin.dto.auth.RefreshRequest;
import com.example.admin.dto.auth.RefreshResponseData;
import com.example.admin.dto.user.UserDto;
import com.example.admin.entity.SysUser;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.security.JwtTokenProvider;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.AuthService;
import com.example.admin.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DbAuthService implements AuthService {

    private final SysUserMapper userMapper;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public DbAuthService(
            SysUserMapper userMapper,
            UserService userService,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponseData login(LoginRequest request) {
        SysUser user = userMapper.selectOne(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(1003, "密码错误");
        }

        UserDto userDto = userService.getById(user.getId());
        List<String> roleCodes = userDto.getRoles() == null ? List.of() : userDto.getRoles().stream().map(r -> r.getCode()).toList();

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        LoginResponseData data = new LoginResponseData();
        data.setToken(accessToken);
        data.setRefreshToken(refreshToken);
        data.setUser(userDto);
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

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BadCredentialsException("refresh token invalid");
        }

        UserDto userDto = userService.getById(userId);
        List<String> roleCodes = userDto.getRoles() == null ? List.of() : userDto.getRoles().stream().map(r -> r.getCode()).toList();

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getUsername(), roleCodes);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

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
        return userService.getById(principal.userId());
    }
}

