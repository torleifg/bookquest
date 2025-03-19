package com.github.torleifg.bookquest.gateway.bibbi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "harvesting.bibbi")
class BibbiProperties {
    private List<GatewayConfig> gateways = new ArrayList<>();

    @Data
    static class GatewayConfig {
        private boolean enabled;
        private String serviceUri;
        private String mapper;
        private Integer ttl;
        private Integer limit;
        private String query;
    }
}