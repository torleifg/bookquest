package com.github.torleifg.semanticsearch.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private final RestClientInterceptor restClientInterceptor;

    @Value("${scheduler.gateway}")
    private String gateway;

    public RestClientConfig(RestClientInterceptor restClientInterceptor) {
        this.restClientInterceptor = restClientInterceptor;
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        if (gateway == null || gateway.isBlank()) {
            throw new IllegalStateException("Gateway property is mandatory");
        }

        builder.requestFactory(new JdkClientHttpRequestFactory());

        return switch (gateway.toLowerCase()) {
            case "bokbasen" -> builder.requestInterceptor(restClientInterceptor).build();
            case "bibbi", "oai-pmh" -> builder.build();
            default -> throw new IllegalStateException("Unknown gateway: " + gateway);
        };
    }
}
