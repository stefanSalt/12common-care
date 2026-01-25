package com.example.admin.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final String CLAIM_TOKEN_TYPE = "token_type";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-token-ttl-seconds:86400}")
    private long accessTokenTtlSeconds;

    @Value("${app.jwt.refresh-token-ttl-seconds:2592000}")
    private long refreshTokenTtlSeconds;

    public String generateAccessToken(long userId, String username, List<String> roles) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenTtlSeconds * 1000);

        return Jwts.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiration(exp)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .claim("userId", userId)
                .claim("username", username)
                .claim("roles", roles)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(long userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshTokenTtlSeconds * 1000);

        return Jwts.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiration(exp)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .claim("userId", userId)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        // JJWT 0.12.x: avoid deprecated setSigningKey/parseClaimsJws.
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenExpired(String token) {
        Date exp = parseToken(token).getExpiration();
        return exp != null && exp.before(new Date());
    }

    public Long getUserIdFromToken(String token) {
        Object userId = parseToken(token).get("userId");
        if (userId == null) {
            return null;
        }
        if (userId instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(userId.toString());
    }

    public String getTokenType(Claims claims) {
        Object type = claims.get(CLAIM_TOKEN_TYPE);
        return type == null ? null : type.toString();
    }

    public boolean isAccessToken(Claims claims) {
        return TOKEN_TYPE_ACCESS.equals(getTokenType(claims));
    }

    public boolean isRefreshToken(Claims claims) {
        return TOKEN_TYPE_REFRESH.equals(getTokenType(claims));
    }

    private SecretKey getSecretKey() {
        // HS256 requires >= 256-bit key (>= 32 bytes).
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}

