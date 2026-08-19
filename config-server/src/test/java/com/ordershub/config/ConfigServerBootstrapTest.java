package com.ordershub.config;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.config.server.EnableConfigServer;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigServerBootstrapTest {

    @Test
    void shouldEnableConfigServer() {
        assertThat(ConfigServerApplication.class)
                .hasAnnotation(EnableConfigServer.class);
    }
}
