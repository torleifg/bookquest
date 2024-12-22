package com.github.torleifg.semanticsearch.adapter.rest_client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.RestClientClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${scheduler.gateway}")
    private String gateway;

    @Value("${bokbasen.client}")
    private String client;

    @Value("${bokbasen.audience}")
    private String audience;

    @Bean
    public RestClient restClient(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager) {
        switch (gateway.toLowerCase()) {
            case "bokbasen" -> {
                final OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
                requestInterceptor.setClientRegistrationIdResolver(it -> client);

                return builder
                        .requestFactory(new JdkClientHttpRequestFactory())
                        .requestInterceptor(requestInterceptor)
                        .build();
            }
            case "bibbi", "oai-pmh" -> {
                return builder
                        .requestFactory(new JdkClientHttpRequestFactory())
                        .build();
            }
            default -> throw new IllegalArgumentException("Unknown gateway: " + gateway);
        }
    }

    @Bean
    @ConditionalOnProperty(prefix = "scheduler", name = "gateway", havingValue = "bokbasen")
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService clientService) {
        final RestClientClientCredentialsTokenResponseClient tokenResponseClient = new RestClientClientCredentialsTokenResponseClient();

        tokenResponseClient.addParametersConverter(grantRequest -> {
            final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.set(OAuth2ParameterNames.AUDIENCE, audience);

            return parameters;
        });

        final OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials(builder -> builder.accessTokenResponseClient(tokenResponseClient))
                .build();

        final AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, clientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }
}
