package com.github.torleifg.bookquest.gateway.bibbi;

import com.github.torleifg.bookquest.core.service.GatewayService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Configuration
class BibbiConfig {

    @Bean
    List<GatewayService> bibbiGateways(BibbiProperties bibbiProperties, BibbiClient bibbiClient) {
        return bibbiProperties.getGateways().stream()
                .filter(BibbiProperties.GatewayConfig::isEnabled)
                .map(config -> createGateway(config, bibbiClient))
                .map(GatewayService.class::cast)
                .toList();
    }

    private BibbiGateway createGateway(BibbiProperties.GatewayConfig gatewayConfig, BibbiClient bibbiClient) {
        final String mapper = gatewayConfig.getMapper();

        /*
        Mapper factory
         */

        final BibbiMapper bibbiMapper = new BibbiDefaultMapper();

        return new BibbiGateway(gatewayConfig, bibbiClient, bibbiMapper);
    }

    @Bean
    BibbiClient bibbiClient(RestClient.Builder builder) {
        final JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(30L));

        final RestClient restClient = builder
                .requestFactory(requestFactory)
                .build();

        return new BibbiClient(restClient);
    }
}
