package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.user.CreateUserRequest;
import com.example.admin.dto.user.SetUserRolesRequest;
import com.example.admin.dto.user.UpdateUserRequest;
import com.example.admin.dto.user.UserDto;

public interface UserService {

    PageResult<UserDto> list(long current, long size);

    UserDto getById(Long id);

    UserDto create(CreateUserRequest request);

    UserDto update(Long id, UpdateUserRequest request);

    void delete(Long id);

    void setRoles(Long userId, SetUserRolesRequest request);
}

