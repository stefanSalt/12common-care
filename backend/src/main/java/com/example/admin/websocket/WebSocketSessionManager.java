package com.example.admin.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class WebSocketSessionManager {

    private final ConcurrentMap<Long, Set<WebSocketSession>> sessionsByUserId = new ConcurrentHashMap<>();

    public void addSession(Long userId, WebSocketSession session) {
        if (userId == null || session == null) {
            return;
        }
        sessionsByUserId.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void removeSession(Long userId, WebSocketSession session) {
        if (userId == null || session == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            sessionsByUserId.remove(userId, sessions);
        }
    }

    public boolean isOnline(Long userId) {
        Set<WebSocketSession> sessions = userId == null ? null : sessionsByUserId.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public void sendToUser(Long userId, String payload) {
        if (userId == null || payload == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByUserId.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                session.sendMessage(new TextMessage(payload));
            } catch (IOException e) {
                log.debug("WebSocket send failed: userId={}, sessionId={}", userId, session.getId(), e);
            }
        }
    }

    public void broadcast(String payload) {
        if (payload == null) {
            return;
        }
        for (var entry : sessionsByUserId.entrySet()) {
            sendToUser(entry.getKey(), payload);
        }
    }
}

