package com.github.torleifg.semanticsearch.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    private final RestClientInterceptor restClientInterceptor;

    public RestClientConfig(RestClientInterceptor restClientInterceptor) {
        this.restClientInterceptor = restClientInterceptor;
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();
    }

    @Bean
    public RestClient OAuth2RestClient(RestClient.Builder builder) {
        return builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .requestInterceptor(restClientInterceptor)
                .build();
    }
}
