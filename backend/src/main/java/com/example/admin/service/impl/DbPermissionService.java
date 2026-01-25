package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.admin.dto.permission.CreatePermissionRequest;
import com.example.admin.dto.permission.PermissionDto;
import com.example.admin.dto.permission.UpdatePermissionRequest;
import com.example.admin.entity.SysPermission;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysRolePermission;
import com.example.admin.entity.SysUserRole;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysPermissionMapper;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysRolePermissionMapper;
import com.example.admin.mapper.SysUserRoleMapper;
import com.example.admin.service.PermissionService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbPermissionService implements PermissionService {

    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    public DbPermissionService(
            SysUserRoleMapper userRoleMapper,
            SysRoleMapper roleMapper,
            SysRolePermissionMapper rolePermissionMapper,
            SysPermissionMapper permissionMapper
    ) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public Set<String> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }

        List<SysUserRole> userRoles = userRoleMapper.selectList(
                Wrappers.lambdaQuery(SysUserRole.class).eq(SysUserRole::getUserId, userId)
        );
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptySet();
        }

        // Filter out deleted roles (SysRole has @TableLogic).
        List<SysRole> roles = roleMapper.selectList(Wrappers.lambdaQuery(SysRole.class).in(SysRole::getId, roleIds));
        Set<Long> activeRoleIds = roles.stream()
                .map(SysRole::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (activeRoleIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
                Wrappers.lambdaQuery(SysRolePermission.class).in(SysRolePermission::getRoleId, activeRoleIds)
        );
        List<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (permissionIds.isEmpty()) {
            return Collections.emptySet();
        }

        List<SysPermission> permissions = permissionMapper.selectList(
                Wrappers.lambdaQuery(SysPermission.class).in(SysPermission::getId, permissionIds)
        );
        return permissions.stream()
                .map(SysPermission::getCode)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public List<PermissionDto> listAll() {
        return permissionMapper.selectList(Wrappers.lambdaQuery(SysPermission.class).orderByAsc(SysPermission::getId))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PermissionDto create(CreatePermissionRequest request) {
        long existing = permissionMapper.selectCount(
                Wrappers.lambdaQuery(SysPermission.class).eq(SysPermission::getCode, request.getCode())
        );
        if (existing > 0) {
            throw new BusinessException(1007, "权限标识已存在");
        }

        SysPermission permission = new SysPermission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setDeleted(0);
        permissionMapper.insert(permission);
        return toDto(permission);
    }

    @Override
    @Transactional
    public PermissionDto update(Long id, UpdatePermissionRequest request) {
        SysPermission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException(1006, "权限不存在");
        }

        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permissionMapper.updateById(permission);
        return toDto(permission);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        int affected = permissionMapper.deleteById(id);
        if (affected == 0) {
            throw new BusinessException(1006, "权限不存在");
        }
    }

    private PermissionDto toDto(SysPermission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}

