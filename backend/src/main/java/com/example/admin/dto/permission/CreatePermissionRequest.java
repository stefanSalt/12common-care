package com.example.admin.dto.permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank(message = "权限标识不能为空")
    private String code;

    @NotBlank(message = "权限名称不能为空")
    private String name;

    private String description;
}

