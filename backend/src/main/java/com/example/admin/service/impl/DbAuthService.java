package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.admin.dto.auth.ChangePasswordRequest;
import com.example.admin.dto.auth.LoginRequest;
import com.example.admin.dto.auth.LoginResponseData;
import com.example.admin.dto.auth.RegisterRequest;
import com.example.admin.dto.auth.RefreshRequest;
import com.example.admin.dto.auth.RefreshResponseData;
import com.example.admin.dto.auth.UpdateMeRequest;
import com.example.admin.dto.file.FileInfoDto;
import com.example.admin.dto.user.UserDto;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysUser;
import com.example.admin.entity.SysUserRole;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.mapper.SysUserRoleMapper;
import com.example.admin.security.JwtTokenProvider;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.AuthService;
import com.example.admin.service.FileService;
import com.example.admin.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DbAuthService implements AuthService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final UserService userService;
    private final FileService fileService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public DbAuthService(
            SysUserMapper userMapper,
            SysRoleMapper roleMapper,
            SysUserRoleMapper userRoleMapper,
            UserService userService,
            FileService fileService,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.userService = userService;
        this.fileService = fileService;
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
    @Transactional
    public LoginResponseData register(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        String username = request.getUsername().trim();
        long existing = userMapper.selectCount(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, username));
        if (existing > 0) {
            throw new BusinessException(1002, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(normalizeBlankToNull(request.getNickname()));
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        SysRole role = roleMapper.selectOne(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getCode, "user"));
        if (role == null) {
            throw new BusinessException(1004, "角色不存在");
        }

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(role.getId());
        userRoleMapper.insert(userRole);

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

    @Override
    public UserDto updateMe(UserPrincipal principal, UpdateMeRequest request) {
        Long userId = requireUserId(principal);
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        user.setNickname(normalizeBlankToNull(request.getNickname()));
        user.setEmail(normalizeBlankToNull(request.getEmail()));
        user.setPhone(normalizeBlankToNull(request.getPhone()));
        userMapper.updateById(user);

        return userService.getById(userId);
    }

    @Override
    public UserDto uploadMyAvatar(UserPrincipal principal, MultipartFile file) {
        Long userId = requireUserId(principal);

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        // Per decision 1A: avatar is stored as a PUBLIC file.
        FileInfoDto uploaded = fileService.upload(userId, file, "PUBLIC");
        user.setAvatarFileId(uploaded.getId());
        userMapper.updateById(user);

        return userService.getById(userId);
    }

    @Override
    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        Long userId = requireUserId(principal);
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException(1003, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
    }

    private Long requireUserId(UserPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        return principal.userId();
    }

    private String normalizeBlankToNull(String value) {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        return v.isBlank() ? null : v;
    }
}
