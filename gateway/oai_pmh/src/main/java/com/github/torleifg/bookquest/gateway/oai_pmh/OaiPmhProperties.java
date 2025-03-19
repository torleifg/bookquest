package com.github.torleifg.bookquest.gateway.oai_pmh;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "harvesting.oai-pmh")
class OaiPmhProperties {
    private List<GatewayConfig> gateways = new ArrayList<>();

    @Data
    static class GatewayConfig {
        private boolean enabled;
        private String serviceUri;
        private long ttl;
        private String mapper;
        private String verb;
        private String metadataPrefix;
        private String set;
    }
}
