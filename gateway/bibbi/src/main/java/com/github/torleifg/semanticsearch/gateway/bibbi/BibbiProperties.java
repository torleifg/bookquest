package com.github.torleifg.semanticsearch.gateway.bibbi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bibbi")
class BibbiProperties {
    private String serviceUri;
    private long ttl;
}
