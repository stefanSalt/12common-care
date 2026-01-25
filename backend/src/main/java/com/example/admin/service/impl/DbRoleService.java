package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.admin.dto.permission.PermissionDto;
import com.example.admin.dto.role.CreateRoleRequest;
import com.example.admin.dto.role.RoleDetailDto;
import com.example.admin.dto.role.SetRolePermissionsRequest;
import com.example.admin.dto.role.UpdateRoleRequest;
import com.example.admin.dto.user.RoleDto;
import com.example.admin.entity.SysPermission;
import com.example.admin.entity.SysRole;
import com.example.admin.entity.SysRolePermission;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysPermissionMapper;
import com.example.admin.mapper.SysRoleMapper;
import com.example.admin.mapper.SysRolePermissionMapper;
import com.example.admin.service.RoleService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbRoleService implements RoleService {

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysPermissionMapper permissionMapper;

    public DbRoleService(
            SysRoleMapper roleMapper,
            SysRolePermissionMapper rolePermissionMapper,
            SysPermissionMapper permissionMapper
    ) {
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    public List<RoleDto> listAll() {
        return roleMapper.selectList(Wrappers.lambdaQuery(SysRole.class).orderByAsc(SysRole::getId))
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public RoleDetailDto getDetail(Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(1004, "角色不存在");
        }

        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
                Wrappers.lambdaQuery(SysRolePermission.class).eq(SysRolePermission::getRoleId, role.getId())
        );
        List<Long> permissionIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<SysPermission> permissions = permissionIds.isEmpty()
                ? Collections.emptyList()
                : permissionMapper.selectList(Wrappers.lambdaQuery(SysPermission.class).in(SysPermission::getId, permissionIds));

        RoleDetailDto dto = new RoleDetailDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setPermissions(permissions.stream().map(this::toPermissionDto).toList());
        return dto;
    }

    @Override
    @Transactional
    public RoleDto create(CreateRoleRequest request) {
        long existing = roleMapper.selectCount(Wrappers.lambdaQuery(SysRole.class).eq(SysRole::getCode, request.getCode()));
        if (existing > 0) {
            throw new BusinessException(1005, "角色编码已存在");
        }

        SysRole role = new SysRole();
        role.setCode(request.getCode());
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setDeleted(0);
        roleMapper.insert(role);
        return toDto(role);
    }

    @Override
    @Transactional
    public RoleDto update(Long id, UpdateRoleRequest request) {
        SysRole role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(1004, "角色不存在");
        }
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        roleMapper.updateById(role);
        return toDto(role);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        int affected = roleMapper.deleteById(id);
        if (affected == 0) {
            throw new BusinessException(1004, "角色不存在");
        }
    }

    @Override
    @Transactional
    public void setPermissions(Long roleId, SetRolePermissionsRequest request) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(1004, "角色不存在");
        }

        List<Long> ids = request.getPermissionIds() == null ? List.of() : request.getPermissionIds();
        Set<Long> distinctIds = new HashSet<>(ids);
        if (!distinctIds.isEmpty()) {
            long count = permissionMapper.selectCount(Wrappers.lambdaQuery(SysPermission.class).in(SysPermission::getId, distinctIds));
            if (count != distinctIds.size()) {
                throw new BusinessException(1006, "权限不存在");
            }
        }

        rolePermissionMapper.delete(Wrappers.lambdaQuery(SysRolePermission.class).eq(SysRolePermission::getRoleId, roleId));
        for (Long permissionId : distinctIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permissionId);
            rolePermissionMapper.insert(rp);
        }
    }

    private RoleDto toDto(SysRole role) {
        RoleDto dto = new RoleDto();
        dto.setId(role.getId());
        dto.setCode(role.getCode());
        dto.setName(role.getName());
        return dto;
    }

    private PermissionDto toPermissionDto(SysPermission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setCode(permission.getCode());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        return dto;
    }
}

