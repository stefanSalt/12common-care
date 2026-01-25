package com.example.admin.service;

import java.util.Set;

public interface PermissionService {
    Set<String> getPermissionsByUserId(Long userId);
}

