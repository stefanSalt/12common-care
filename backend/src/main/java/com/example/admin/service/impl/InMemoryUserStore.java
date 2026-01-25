package com.example.admin.service.impl;

import com.example.admin.dto.user.RoleDto;
import com.example.admin.dto.user.UserDto;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("inmem")
public class InMemoryUserStore {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, UserRecord> usersByUsername = new ConcurrentHashMap<>();
    private final Map<Long, UserRecord> usersById = new ConcurrentHashMap<>();

    public InMemoryUserStore(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        // Scaffolding only: a built-in admin user to let frontend / API smoke tests run.
        RoleDto adminRole = new RoleDto();
        adminRole.setId(10L);
        adminRole.setCode("admin");
        adminRole.setName("管理员");

        UserRecord admin = UserRecord.builder()
                .id(1L)
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .nickname("Admin")
                .email("admin@example.com")
                .phone("13800000000")
                .status(1)
                .roles(List.of(adminRole))
                .permissions(Set.of(
                        "user:list", "user:create", "user:update", "user:delete",
                        "role:list", "role:create", "role:update", "role:delete",
                        "permission:list", "permission:create", "permission:update", "permission:delete",
                        "notification:announce"
                ))
                .build();

        put(admin);
    }

    public UserRecord findByUsername(String username) {
        if (username == null) {
            return null;
        }
        return usersByUsername.get(username);
    }

    public UserRecord findById(Long id) {
        if (id == null) {
            return null;
        }
        return usersById.get(id);
    }

    private void put(UserRecord user) {
        usersByUsername.put(user.username(), user);
        usersById.put(user.id(), user);
    }

    @Builder
    public record UserRecord(
            Long id,
            String username,
            String passwordHash,
            String nickname,
            String email,
            String phone,
            Integer status,
            List<RoleDto> roles,
            Set<String> permissions
    ) {
        public List<String> roleCodes() {
            if (roles == null) {
                return List.of();
            }
            return roles.stream().map(RoleDto::getCode).toList();
        }

        public UserDto toUserDto() {
            UserDto dto = new UserDto();
            dto.setId(id);
            dto.setUsername(username);
            dto.setNickname(nickname);
            dto.setEmail(email);
            dto.setPhone(phone);
            dto.setStatus(status);
            dto.setRoles(roles);
            dto.setPermissions(permissions == null ? List.of() : permissions.stream().sorted().toList());
            return dto;
        }
    }
}
