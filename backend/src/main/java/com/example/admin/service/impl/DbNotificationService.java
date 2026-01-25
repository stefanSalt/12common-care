package com.example.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.admin.common.PageResult;
import com.example.admin.dto.notification.AnnounceRequest;
import com.example.admin.dto.notification.NotificationDto;
import com.example.admin.entity.SysNotification;
import com.example.admin.entity.SysUser;
import com.example.admin.exception.BusinessException;
import com.example.admin.mapper.SysNotificationMapper;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.service.NotificationService;
import com.example.admin.websocket.WebSocketSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DbNotificationService implements NotificationService {

    private final SysNotificationMapper notificationMapper;
    private final SysUserMapper userMapper;
    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public DbNotificationService(
            SysNotificationMapper notificationMapper,
            SysUserMapper userMapper,
            WebSocketSessionManager sessionManager,
            ObjectMapper objectMapper
    ) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<NotificationDto> listMyNotifications(Long userId, long current, long size) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }

        Page<SysNotification> page = notificationMapper.selectPage(
                new Page<>(current, size),
                Wrappers.lambdaQuery(SysNotification.class)
                        .eq(SysNotification::getUserId, userId)
                        .orderByDesc(SysNotification::getId)
        );

        List<NotificationDto> records = page.getRecords().stream().map(this::toDto).toList();

        PageResult<NotificationDto> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (notificationId == null) {
            throw new BusinessException(400, "id不能为空");
        }

        SysNotification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }
        if (!userId.equals(notification.getUserId())) {
            throw new AccessDeniedException("权限不足");
        }

        if (notification.getIsRead() != null && notification.getIsRead() == 1) {
            return;
        }

        SysNotification update = new SysNotification();
        update.setId(notificationId);
        update.setIsRead(1);
        notificationMapper.updateById(update);
    }

    @Override
    public void announce(Long adminUserId, AnnounceRequest request) {
        // adminUserId is validated by @RequiresPermission on controller, but keep it consistent with other services.
        if (adminUserId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        if (request == null) {
            throw new BusinessException(400, "请求不能为空");
        }

        String title = request.getTitle();
        String content = request.getContent();

        List<SysUser> users = userMapper.selectList(Wrappers.lambdaQuery(SysUser.class).select(SysUser::getId));
        for (SysUser user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }
            SysNotification n = new SysNotification();
            n.setId(IdWorker.getId());
            n.setUserId(user.getId());
            n.setTitle(title);
            n.setContent(content);
            n.setType("ANNOUNCEMENT");
            n.setIsRead(0);
            notificationMapper.insert(n);

            pushToUser(user.getId(), toDto(n));
        }
    }

    @Override
    public void sendToUser(Long userId, String title, String content, String type) {
        if (userId == null) {
            throw new BusinessException(400, "userId不能为空");
        }
        String normalizedType = normalizeType(type);

        SysNotification n = new SysNotification();
        n.setId(IdWorker.getId());
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(normalizedType);
        n.setIsRead(0);
        notificationMapper.insert(n);

        pushToUser(userId, toDto(n));
    }

    @Override
    public List<NotificationDto> listUnread(Long userId, int limit) {
        if (userId == null) {
            throw new AuthenticationCredentialsNotFoundException("未登录");
        }
        int pageSize = Math.max(1, limit);
        Page<SysNotification> page = notificationMapper.selectPage(
                new Page<>(1, pageSize),
                Wrappers.lambdaQuery(SysNotification.class)
                        .eq(SysNotification::getUserId, userId)
                        .eq(SysNotification::getIsRead, 0)
                        .orderByDesc(SysNotification::getId)
        );
        return page.getRecords().stream().map(this::toDto).toList();
    }

    private NotificationDto toDto(SysNotification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setTitle(n.getTitle());
        dto.setContent(n.getContent());
        dto.setType(n.getType());
        dto.setIsRead(n.getIsRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "SYSTEM";
        }
        String t = type.trim().toUpperCase(Locale.ROOT);
        return switch (t) {
            case "SYSTEM", "BUSINESS", "ANNOUNCEMENT" -> t;
            default -> throw new BusinessException(400, "type参数错误");
        };
    }

    private void pushToUser(Long userId, NotificationDto notification) {
        if (!sessionManager.isOnline(userId)) {
            return;
        }
        try {
            String payload = objectMapper.writeValueAsString(new WsMessage("notification", notification));
            sessionManager.sendToUser(userId, payload);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private record WsMessage(String type, Object data) {}
}

