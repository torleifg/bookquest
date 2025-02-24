package com.github.torleifg.semanticsearch.gateway.bokbasen;

import com.github.torleifg.semanticsearch.book.repository.ResumptionTokenRepository;
import com.github.torleifg.semanticsearch.book.service.MetadataGateway;
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
@ConditionalOnProperty(prefix = "harvesting", name = "gateway", havingValue = "bokbasen")
class BokbasenConfig {

    @Bean
    MetadataGateway metadataGateway(BokbasenClient bokbasenClient, BokbasenMapper bokbasenMapper, BokbasenProperties bokbasenProperties, ResumptionTokenRepository resumptionTokenRepository) {
        return new BokbasenGateway(bokbasenClient, bokbasenMapper, bokbasenProperties, resumptionTokenRepository);
    }

    @Bean
    BokbasenMapper bokbasenMapper(BokbasenProperties bokbasenProperties) {
        final String mapper = bokbasenProperties.getMapper();

        /*
        Mapper factory
         */

        return new BokbasenDefaultMapper();
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
