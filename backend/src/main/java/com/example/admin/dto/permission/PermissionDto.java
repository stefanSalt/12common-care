package com.example.admin.dto.permission;

import lombok.Data;

@Data
public class PermissionDto {
    private Long id;
    private String code;
    private String name;
    private String description;
}

