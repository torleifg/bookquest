package com.github.torleifg.semanticsearchonnx.gateway.oai_pmh;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "oai-pmh")
class OaiPmhProperties {
    private String serviceUri;
    private long ttl;
}
