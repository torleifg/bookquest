package com.github.torleifg.bookquest.gateway.bokbasen;

import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.GatewayService;
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

import java.util.List;

@Configuration
class BokbasenConfig {

    @Bean
    List<GatewayService> bokbasenGateways(BokbasenClient bokbasenClient, BokbasenProperties bokbasenProperties, ResumptionTokenRepository resumptionTokenRepository) {
        return bokbasenProperties.getGateways().stream()
                .filter(BokbasenProperties.GatewayConfig::isEnabled)
                .map(config -> createGateway(config, bokbasenClient, resumptionTokenRepository))
                .map(GatewayService.class::cast)
                .toList();
    }

    private BokbasenGateway createGateway(BokbasenProperties.GatewayConfig gatewayConfig, BokbasenClient bokbasenClient, ResumptionTokenRepository resumptionTokenRepository) {
        final String mapper = gatewayConfig.getMapper();

        /*
        Mapper factory
         */

        final BokbasenMapper bokbasenMapper = new BokbasenDefaultMapper();

        return new BokbasenGateway(gatewayConfig, bokbasenClient, bokbasenMapper, resumptionTokenRepository);
    }

    @Bean
    BokbasenClient bokbasenClient(RestClient.Builder builder, OAuth2AuthorizedClientManager authorizedClientManager, BokbasenProperties bokbasenProperties) {
        final OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        requestInterceptor.setClientRegistrationIdResolver(it -> bokbasenProperties.getClient());

        final RestClient restClient = builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .requestInterceptor(requestInterceptor)
                .build();

        return new BokbasenClient(restClient);
    }

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository, OAuth2AuthorizedClientService clientService, BokbasenProperties bokbasenProperties) {
        final RestClientClientCredentialsTokenResponseClient tokenResponseClient = new RestClientClientCredentialsTokenResponseClient();

        tokenResponseClient.addParametersConverter(grantRequest -> {
            final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
            parameters.set(OAuth2ParameterNames.AUDIENCE, bokbasenProperties.getAudience());

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
