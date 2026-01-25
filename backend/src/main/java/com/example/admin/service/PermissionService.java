package com.example.admin.service;

import com.example.admin.dto.permission.CreatePermissionRequest;
import com.example.admin.dto.permission.PermissionDto;
import com.example.admin.dto.permission.UpdatePermissionRequest;
import java.util.List;
import java.util.Set;

public interface PermissionService {
    Set<String> getPermissionsByUserId(Long userId);

    List<PermissionDto> listAll();

    PermissionDto create(CreatePermissionRequest request);

    PermissionDto update(Long id, UpdatePermissionRequest request);

    void delete(Long id);
}
