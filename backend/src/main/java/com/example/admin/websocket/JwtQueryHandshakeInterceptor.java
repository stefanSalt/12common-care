package com.example.admin.websocket;

import com.example.admin.entity.SysUser;
import com.example.admin.mapper.SysUserMapper;
import com.example.admin.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class JwtQueryHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final SysUserMapper userMapper;

    public JwtQueryHandshakeInterceptor(JwtTokenProvider jwtTokenProvider, SysUserMapper userMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        if (token == null || token.isBlank()) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(token);
        } catch (Exception e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (!jwtTokenProvider.isAccessToken(claims)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        Long userId = getUserIdFromClaims(claims);
        if (userId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put("userId", userId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }

    private Long getUserIdFromClaims(Claims claims) {
        Object userId = claims.get("userId");
        if (userId == null) {
            return null;
        }
        if (userId instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(userId.toString());
        } catch (Exception e) {
            return null;
        }
    }
}

