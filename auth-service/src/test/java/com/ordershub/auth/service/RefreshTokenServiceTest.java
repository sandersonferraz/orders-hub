package com.ordershub.auth.service;

import com.ordershub.auth.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    StringRedisTemplate redis;

    @Mock
    ValueOperations<String, String> valueOps;

    RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(redis, Duration.ofDays(7));
        when(redis.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void shouldSaveTokenWithTtl() {
        String token = service.create(1L);

        assertThat(token).isNotBlank();
        verify(valueOps).set("refresh:" + token, "1", Duration.ofDays(7));
    }

    @Test
    void shouldReturnUserIdAndDeleteToken() {
        when(valueOps.get("refresh:abc")).thenReturn("42");

        Long userId = service.validateAndRotate("abc");

        assertThat(userId).isEqualTo(42L);
        verify(redis).delete("refresh:abc");
    }

    @Test
    void shouldThrowExceptionWhenTokenDoesNotExist() {
        when(valueOps.get("refresh:abc")).thenReturn(null);

        assertThatThrownBy(() -> service.validateAndRotate("abc"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }
}
