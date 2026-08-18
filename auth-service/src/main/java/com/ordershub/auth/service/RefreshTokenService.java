package com.ordershub.auth.service;

import com.ordershub.auth.exception.InvalidRefreshTokenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate redis;
    private final Duration refreshTtl;

    public RefreshTokenService(StringRedisTemplate redis,
                               @Value("${app.jwt.refresh-ttl}") Duration refreshTtl) {
        this.redis = redis;
        this.refreshTtl = refreshTtl;
    }

    public String create(Long userId) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set("refresh:" + token, userId.toString(), refreshTtl);
        return token;
    }

    public Long validateAndRotate(String token) {
        String userId = redis.opsForValue().get("refresh:" + token);
        if (userId == null) {
            throw new InvalidRefreshTokenException();
        }
        redis.delete("refresh:" + token); // rotação: token de uso único
        return Long.valueOf(userId);
    }
}