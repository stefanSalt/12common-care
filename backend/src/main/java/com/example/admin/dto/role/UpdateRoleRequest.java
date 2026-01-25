package com.example.admin.dto.role;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotBlank(message = "角色名称不能为空")
    private String name;

    private String description;
}

