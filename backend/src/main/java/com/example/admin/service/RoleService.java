package com.example.admin.service;

import com.example.admin.dto.role.CreateRoleRequest;
import com.example.admin.dto.role.RoleDetailDto;
import com.example.admin.dto.role.SetRolePermissionsRequest;
import com.example.admin.dto.role.UpdateRoleRequest;
import com.example.admin.dto.user.RoleDto;
import java.util.List;

public interface RoleService {

    List<RoleDto> listAll();

    RoleDetailDto getDetail(Long id);

    RoleDto create(CreateRoleRequest request);

    RoleDto update(Long id, UpdateRoleRequest request);

    void delete(Long id);

    void setPermissions(Long roleId, SetRolePermissionsRequest request);
}

