package com.example.admin.dto.role;

import com.example.admin.dto.permission.PermissionDto;
import java.util.List;
import lombok.Data;

@Data
public class RoleDetailDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private List<PermissionDto> permissions;
}

