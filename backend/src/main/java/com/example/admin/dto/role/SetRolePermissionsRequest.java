package com.example.admin.dto.role;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class SetRolePermissionsRequest {
    @NotNull(message = "permissionIds不能为空")
    private List<Long> permissionIds;
}

