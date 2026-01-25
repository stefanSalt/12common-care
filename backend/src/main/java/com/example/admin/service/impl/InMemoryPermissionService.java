package com.example.admin.service.impl;

import com.example.admin.service.PermissionService;
import java.util.Collections;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
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
}

