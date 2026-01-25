package com.example.admin.service;

import com.example.admin.common.PageResult;
import com.example.admin.dto.notification.AnnounceRequest;
import com.example.admin.dto.notification.NotificationDto;
import java.util.List;

public interface NotificationService {

    PageResult<NotificationDto> listMyNotifications(Long userId, long current, long size);

    void markRead(Long userId, Long notificationId);

    void announce(Long adminUserId, AnnounceRequest request);

    void sendToUser(Long userId, String title, String content, String type);

    List<NotificationDto> listUnread(Long userId, int limit);
}

