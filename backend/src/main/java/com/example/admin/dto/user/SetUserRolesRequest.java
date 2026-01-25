package com.example.admin.dto.user;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class SetUserRolesRequest {
    @NotNull(message = "roleIds不能为空")
    private List<Long> roleIds;
}

