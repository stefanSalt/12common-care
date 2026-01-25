package com.example.admin.websocket;

import com.example.admin.dto.notification.NotificationDto;
import com.example.admin.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private static final int INIT_UNREAD_LIMIT = 50;

    private final WebSocketSessionManager sessionManager;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationWebSocketHandler(
            WebSocketSessionManager sessionManager,
            NotificationService notificationService,
            ObjectMapper objectMapper
    ) {
        this.sessionManager = sessionManager;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId == null) {
            safeClose(session, CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        sessionManager.addSession(userId, session);

        try {
            List<NotificationDto> unread = notificationService.listUnread(userId, INIT_UNREAD_LIMIT);
            String payload = objectMapper.writeValueAsString(new WsMessage("init", unread));
            session.sendMessage(new TextMessage(payload));
        } catch (Exception e) {
            log.debug("WebSocket init send failed: userId={}, sessionId={}", userId, session.getId(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionManager.removeSession(userId, session);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Long userId = getUserId(session);
        if (userId != null) {
            sessionManager.removeSession(userId, session);
        }
    }

    private Long getUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Number n) {
            return n.longValue();
        }
        if (userId == null) {
            return null;
        }
        try {
            return Long.parseLong(userId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private void safeClose(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (Exception ignored) {
            // ignore
        }
    }

    private record WsMessage(String type, Object data) {}
}

