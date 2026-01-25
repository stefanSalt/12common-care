package com.example.admin.service.impl;

import com.example.admin.dto.permission.CreatePermissionRequest;
import com.example.admin.dto.permission.PermissionDto;
import com.example.admin.dto.permission.UpdatePermissionRequest;
import com.example.admin.service.PermissionService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("inmem")
public class InMemoryPermissionService implements PermissionService {

    private final InMemoryUserStore userStore;

    public InMemoryPermissionService(InMemoryUserStore userStore) {
        this.userStore = userStore;
    }

    @Override
    public Set<String> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        InMemoryUserStore.UserRecord user = userStore.findById(userId);
        if (user == null) {
            return Collections.emptySet();
        }
        return user.permissions();
    }

    @Override
    public List<PermissionDto> listAll() {
        return List.of();
    }

    @Override
    public PermissionDto create(CreatePermissionRequest request) {
        throw new UnsupportedOperationException("inmem profile does not support permission CRUD");
    }

    @Override
    public PermissionDto update(Long id, UpdatePermissionRequest request) {
        throw new UnsupportedOperationException("inmem profile does not support permission CRUD");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("inmem profile does not support permission CRUD");
    }
}
