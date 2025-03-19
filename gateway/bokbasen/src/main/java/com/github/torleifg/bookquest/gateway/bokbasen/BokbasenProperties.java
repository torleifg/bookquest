package com.github.torleifg.bookquest.gateway.bokbasen;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "harvesting.bokbasen")
class BokbasenProperties {
    private String audience;
    private String client;
    private List<GatewayConfig> gateways = new ArrayList<>();

    @Data
    static class GatewayConfig {
        private boolean enabled;
        private String serviceUri;
        private String mapper;
        private String subscription;
        private String after;
        private Integer pagesize;
    }
}
