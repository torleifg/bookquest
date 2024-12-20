package com.github.torleifg.semanticsearch.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class RestClientInterceptor implements ClientHttpRequestInterceptor {
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${bokbasen.client}")
    private String client;

    public RestClientInterceptor(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        final OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(client)
                .principal("a string")
                .build();


        final OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null) {
            log.error("Failed to authorize client");

            return execution.execute(request, body);
        }

        final OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        request.getHeaders().setBearerAuth(accessToken.getTokenValue());

        return execution.execute(request, body);
    }
}