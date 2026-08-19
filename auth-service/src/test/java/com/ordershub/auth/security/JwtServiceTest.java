package com.ordershub.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwt = new JwtService(
            "test-secret-que-eh-longa-o-suficiente-0123456789",
            Duration.ofMinutes(15));

    @Test
    void shouldRoundtripSubjectAndUserId() {
        String token = jwt.generate(1L, "user@example.com");

        Claims claims = jwt.parse(token);

        assertThat(claims.getSubject()).isEqualTo("user@example.com");
        assertThat(claims.get("userId", Number.class).longValue()).isEqualTo(1L);
    }

    @Test
    void shouldThrowJwtExceptionWithInvalidToken() {
        assertThatThrownBy(() -> jwt.parse("token-invalido"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void shouldProduceSignedNonEmptyToken() {
        String token = jwt.generate(2L, "outro@example.com");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }
}
