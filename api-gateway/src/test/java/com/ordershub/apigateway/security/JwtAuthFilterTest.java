package com.ordershub.apigateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    static final String SECRET = "test-secret-que-eh-longa-o-suficiente-0123456789";

    @Mock ServerWebExchange exchange;
    @Mock ServerWebExchange.Builder exchangeBuilder;
    @Mock ServerWebExchange mutatedExchange;
    @Mock ServerHttpRequest request;
    @Mock ServerHttpRequest.Builder requestBuilder;
    @Mock ServerHttpResponse response;
    @Mock GatewayFilterChain chain;
    @Mock RequestPath path;

    JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(SECRET);
        lenient().when(exchange.getRequest()).thenReturn(request);
        lenient().when(exchange.getResponse()).thenReturn(response);
        lenient().when(request.getPath()).thenReturn(path);
        lenient().when(response.setComplete()).thenReturn(Mono.empty());
        lenient().when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private String validToken(long userId) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("user@example.com")
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(key)
                .compact();
    }

    @Test
    void shouldAllowPublicPath() {
        when(path.value()).thenReturn("/auth/login");

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        verify(response, never()).setStatusCode(any());
    }

    @Test
    void shouldReturn401WithoutAuthorization() {
        when(path.value()).thenReturn("/products");
        when(request.getHeaders()).thenReturn(new HttpHeaders());

        filter.filter(exchange, chain).block();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(response).setComplete();
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldReturn401WithInvalidToken() {
        when(path.value()).thenReturn("/products");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer token-invalido");
        when(request.getHeaders()).thenReturn(headers);

        filter.filter(exchange, chain).block();

        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldInjectXUserIdWithValidToken() {
        when(path.value()).thenReturn("/products");
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + validToken(123L));
        when(request.getHeaders()).thenReturn(headers);

        when(exchange.mutate()).thenReturn(exchangeBuilder);
        when(exchangeBuilder.request(ArgumentMatchers.<Consumer<ServerHttpRequest.Builder>>any())).thenAnswer(invocation -> {
            Consumer<ServerHttpRequest.Builder> consumer = invocation.getArgument(0);
            consumer.accept(requestBuilder);
            return exchangeBuilder;
        });
        when(exchangeBuilder.build()).thenReturn(mutatedExchange);

        filter.filter(exchange, chain).block();

        verify(requestBuilder).header("X-User-Id", "123");
        verify(chain).filter(mutatedExchange);
        verify(chain, never()).filter(exchange);
    }

    @Test
    void shouldReturnMinusOne() {
        assertThat(filter.getOrder()).isEqualTo(-1);
    }
}
