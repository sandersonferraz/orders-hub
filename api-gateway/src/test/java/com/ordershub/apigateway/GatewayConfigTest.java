package com.ordershub.apigateway;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayConfigTest {

    private final GatewayConfig config = new GatewayConfig();

    @Mock ServerWebExchange exchange;
    @Mock ServerHttpRequest request;

    @Test
    void shouldReturnIpFromRemoteAddress() throws Exception {
        when(exchange.getRequest()).thenReturn(request);
        InetAddress addr = InetAddress.getByAddress(new byte[]{10, 0, 0, 1});
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(addr, 8080));

        KeyResolver resolver = config.ipKeyResolver();
        String key = resolver.resolve(exchange).block();

        assertThat(key).isEqualTo("10.0.0.1");
    }

    @Test
    void shouldReturnUnknownWithoutRemoteAddress() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getRemoteAddress()).thenReturn(null);

        KeyResolver resolver = config.ipKeyResolver();
        String key = resolver.resolve(exchange).block();

        assertThat(key).isEqualTo("unknown");
    }
}
