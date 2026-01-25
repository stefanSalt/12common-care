package com.example.admin.dto.user;

import java.util.List;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String phone;
    private Integer status;
    private List<RoleDto> roles;
    private List<String> permissions;
}

