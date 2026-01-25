package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.user.CreateUserRequest;
import com.example.admin.dto.user.RoleDto;
import com.example.admin.dto.user.SetUserRolesRequest;
import com.example.admin.dto.user.UpdateUserRequest;
import com.example.admin.dto.user.UserDto;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysUser;
import com.example.admin.entity.SysUserRole;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.mapper.SysUserRoleMapper;
import com.example.admin.service.PermissionService;
import com.example.admin.service.UserService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbUserService implements UserService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final PermissionService permissionService;
    private final PasswordEncoder passwordEncoder;

    public DbUserService(
            SysUserMapper userMapper,
            SysUserRoleMapper userRoleMapper,
            SysRoleMapper roleMapper,
            PermissionService permissionService,
            PasswordEncoder passwordEncoder
    ) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.permissionService = permissionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserDto> list(long current, long size) {
        Page<SysUser> page = userMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(SysUser.class).orderByAsc(SysUser::getId)
        );

        List<UserDto> records = page.getRecords().stream().map(this::toDtoNoAuthInfo).toList();

        PageResult<UserDto> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public UserDto getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        List<RoleDto> roles = getRolesByUserId(user.getId());
        Set<String> permissions = permissionService.getPermissionsByUserId(user.getId());

        UserDto dto = toDtoNoAuthInfo(user);
        dto.setRoles(roles);
        dto.setPermissions(permissions.stream().sorted().toList());
        return dto;
    }

    @Override
    @Transactional
    public UserDto create(CreateUserRequest request) {
        long existing = userMapper.selectCount(Wrappers.lambdaQuery(SysUser.class).eq(SysUser::getUsername, request.getUsername()));
        if (existing > 0) {
            throw new BusinessException(1002, "用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        user.setDeleted(0);
        userMapper.insert(user);
        return getById(user.getId());
    }

    @Override
    @Transactional
    public UserDto update(Long id, UpdateUserRequest request) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        userMapper.updateById(user);
        return getById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        int affected = userMapper.deleteById(id);
        if (affected == 0) {
            throw new BusinessException(1001, "用户不存在");
        }
    }

    @Override
    @Transactional
    public void setRoles(Long userId, SetUserRolesRequest request) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(1001, "用户不存在");
        }

        List<Long> ids = request.getRoleIds() == null ? List.of() : request.getRoleIds();
        Set<Long> distinctIds = new HashSet<>(ids);
        if (!distinctIds.isEmpty()) {
            long count = roleMapper.selectCount(Wrappers.lambdaQuery(SysRole.class).in(SysRole::getId, distinctIds));
            if (count != distinctIds.size()) {
                throw new BusinessException(1004, "角色不存在");
            }
        }

        userRoleMapper.delete(Wrappers.lambdaQuery(SysUserRole.class).eq(SysUserRole::getUserId, userId));
        for (Long roleId : distinctIds) {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    private UserDto toDtoNoAuthInfo(SysUser user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setAvatarFileId(user.getAvatarFileId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setRoles(Collections.emptyList());
        dto.setPermissions(Collections.emptyList());
        return dto;
    }

    private List<RoleDto> getRolesByUserId(Long userId) {
        List<SysUserRole> userRoles = userRoleMapper.selectList(
                Wrappers.lambdaQuery(SysUserRole.class).eq(SysUserRole::getUserId, userId)
        );
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }

        // SysRole has @TableLogic: deleted roles won't be returned.
        List<SysRole> roles = roleMapper.selectList(Wrappers.lambdaQuery(SysRole.class).in(SysRole::getId, roleIds));
        return roles.stream().map(this::toRoleDto).toList();
    }

    private RoleDto toRoleDto(SysRole role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        return dto;
    }
}
