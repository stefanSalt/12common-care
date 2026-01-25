package com.example.admin.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void accessTokenContainsExpectedClaims() {
        String token = jwtTokenProvider.generateAccessToken(1L, "admin", List.of("admin"));

        Claims claims = jwtTokenProvider.parseToken(token);
        assertThat(jwtTokenProvider.isAccessToken(claims)).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(claims)).isFalse();
        assertThat(claims.get("userId")).isEqualTo(1);
        assertThat(claims.get("username")).isEqualTo("admin");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertThat(roles).containsExactly("admin");
    }

    @Test
    void refreshTokenContainsExpectedClaims() {
        String token = jwtTokenProvider.generateRefreshToken(1L);

        Claims claims = jwtTokenProvider.parseToken(token);
        assertThat(jwtTokenProvider.isRefreshToken(claims)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(claims)).isFalse();
        assertThat(claims.get("userId")).isEqualTo(1);
    }

    @Test
    void tamperedTokenFailsSignatureValidation() {
        String token = jwtTokenProvider.generateAccessToken(1L, "admin", List.of("admin"));

        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);

        // Tamper payload segment (base64url) to break signature verification.
        char[] payloadChars = parts[1].toCharArray();
        payloadChars[0] = payloadChars[0] == 'a' ? 'b' : 'a';
        String tampered = parts[0] + "." + new String(payloadChars) + "." + parts[2];

        assertThatThrownBy(() -> jwtTokenProvider.parseToken(tampered))
                .isInstanceOf(JwtException.class);
    }
}
