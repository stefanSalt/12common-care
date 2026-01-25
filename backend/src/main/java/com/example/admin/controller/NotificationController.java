package com.example.admin.controller;

import com.example.admin.common.PageResult;
import com.example.admin.common.Result;
import com.example.admin.dto.notification.AnnounceRequest;
import com.example.admin.dto.notification.NotificationDto;
import com.example.admin.security.RequiresPermission;
import com.example.admin.security.UserPrincipal;
import com.example.admin.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<PageResult<NotificationDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size
    ) {
        Long userId = principal == null ? null : principal.userId();
        return Result.ok(notificationService.listMyNotifications(userId, current, size));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long id
    ) {
        Long userId = principal == null ? null : principal.userId();
        notificationService.markRead(userId, id);
        return Result.ok(null);
    }

    @PostMapping("/announce")
    @RequiresPermission("notification:announce")
    public Result<Void> announce(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AnnounceRequest request
    ) {
        Long userId = principal == null ? null : principal.userId();
        notificationService.announce(userId, request);
        return Result.ok(null);
    }
}

