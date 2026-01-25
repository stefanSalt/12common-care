package com.example.admin.security;

import com.example.admin.service.PermissionService;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    private final PermissionService permissionService;

    public PermissionAspect(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@annotation(requiresPermission)")
    public Object around(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        String permission = requiresPermission.value();
        if (permission == null || permission.isBlank()) {
            return joinPoint.proceed();
        }

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }

        Set<String> permissions = permissionService.getPermissionsByUserId(principal.userId());
        if (!permissions.contains(permission)) {
            log.debug("Permission denied: userId={}, need={}, has={}", principal.userId(), permission, permissions);
            throw new AccessDeniedException("权限不足");
        }

        return joinPoint.proceed();
    }
}

