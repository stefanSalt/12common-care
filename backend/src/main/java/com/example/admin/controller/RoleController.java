package com.example.admin.controller;

import com.example.admin.common.Result;
import com.example.admin.dto.role.CreateRoleRequest;
import com.example.admin.dto.role.RoleDetailDto;
import com.example.admin.dto.role.SetRolePermissionsRequest;
import com.example.admin.dto.role.UpdateRoleRequest;
import com.example.admin.dto.user.RoleDto;
import com.example.admin.security.RequiresPermission;
import com.example.admin.service.RoleService;
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
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @RequiresPermission("role:list")
    public Result<List<RoleDto>> listAll() {
        return Result.ok(roleService.listAll());
    }

    @GetMapping("/{id}")
    @RequiresPermission("role:list")
    public Result<RoleDetailDto> getDetail(@PathVariable Long id) {
        return Result.ok(roleService.getDetail(id));
    }

    @PostMapping
    @RequiresPermission("role:create")
    public Result<RoleDto> create(@Valid @RequestBody CreateRoleRequest request) {
        return Result.ok(roleService.create(request));
    }

    @PutMapping("/{id}")
    @RequiresPermission("role:update")
    public Result<RoleDto> update(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        return Result.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok(null);
    }

    @PutMapping("/{id}/permissions")
    @RequiresPermission("role:update")
    public Result<Void> setPermissions(@PathVariable Long id, @Valid @RequestBody SetRolePermissionsRequest request) {
        roleService.setPermissions(id, request);
        return Result.ok(null);
    }
}

