package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.user.CreateUserRequest;
import com.example.admin.dto.user.SetUserRolesRequest;
import com.example.admin.dto.user.UpdateUserRequest;
import com.example.admin.dto.user.UserDto;
import com.example.admin.security.RequiresPermission;
import com.example.admin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @RequiresPermission("user:list")
    public Result<PageResult<UserDto>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String excludeRoleCode
    ) {
        return Result.ok(userService.list(current, size, roleCode, excludeRoleCode));
    }

    @GetMapping("/{id}")
    @RequiresPermission("user:list")
    public Result<UserDto> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    @RequiresPermission("user:create")
    public Result<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        return Result.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    @RequiresPermission("user:update")
    public Result<UserDto> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return Result.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok(null);
    }

    @PutMapping("/{id}/roles")
    @RequiresPermission("user:update")
    public Result<Void> setRoles(@PathVariable Long id, @Valid @RequestBody SetUserRolesRequest request) {
        userService.setRoles(id, request);
        return Result.ok(null);
    }
}
