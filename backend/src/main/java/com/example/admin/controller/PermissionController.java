package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.dto.permission.CreatePermissionRequest;
import com.example.admin.dto.permission.PermissionDto;
import com.example.admin.dto.permission.UpdatePermissionRequest;
import com.example.admin.security.RequiresPermission;
import com.example.admin.service.PermissionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @RequiresPermission("permission:list")
    public Result<List<PermissionDto>> listAll() {
        return Result.ok(permissionService.listAll());
    }

    @PostMapping
    @RequiresPermission("permission:create")
    public Result<PermissionDto> create(@Valid @RequestBody CreatePermissionRequest request) {
        return Result.ok(permissionService.create(request));
    }

    @PutMapping("/{id}")
    @RequiresPermission("permission:update")
    public Result<PermissionDto> update(@PathVariable Long id, @Valid @RequestBody UpdatePermissionRequest request) {
        return Result.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("permission:delete")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.ok(null);
    }
}

