package com.example.admin.config;

import com.example.admin.websocket.JwtQueryHandshakeInterceptor;
import com.example.admin.websocket.NotificationWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final JwtQueryHandshakeInterceptor jwtQueryHandshakeInterceptor;

    public WebSocketConfig(
            NotificationWebSocketHandler notificationWebSocketHandler,
            JwtQueryHandshakeInterceptor jwtQueryHandshakeInterceptor
    ) {
        this.notificationWebSocketHandler = notificationWebSocketHandler;
        this.jwtQueryHandshakeInterceptor = jwtQueryHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationWebSocketHandler, "/ws/notification")
                .addInterceptors(jwtQueryHandshakeInterceptor)
                .setAllowedOriginPatterns("*");
    }
}

