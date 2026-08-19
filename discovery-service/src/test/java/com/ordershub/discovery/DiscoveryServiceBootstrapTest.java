package com.ordershub.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryServiceBootstrapTest {

    @Test
    void shouldEnableEurekaServer() {
        assertThat(DiscoveryServiceApplication.class)
                .hasAnnotation(EnableEurekaServer.class);
    }
}
