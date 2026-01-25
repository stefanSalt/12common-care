package com.example.admin.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePermissionRequest {
    @NotBlank(message = "权限名称不能为空")
    private String name;

    private String description;
}

