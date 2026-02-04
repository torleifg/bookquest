package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.service.GatewayService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

@Configuration
class OaiPmhConfig {

    @Bean
    List<GatewayService> oaiPmhGateways(OaiPmhClient oaiPmhClient, OaiPmhProperties oaiPmhProperties) {
        return oaiPmhProperties.getGateways().stream()
                .filter(OaiPmhProperties.GatewayConfig::isEnabled)
                .map(config -> createGateway(config, oaiPmhClient))
                .map(GatewayService.class::cast)
                .toList();
    }

    private OaiPmhGateway createGateway(OaiPmhProperties.GatewayConfig gatewayConfig, OaiPmhClient oaiPmhClient) {
        final String mapper = gatewayConfig.getMapper();

        /*
        Mapper factory
         */

        final OaiPmhMapper oaiPmhMapper = new OaiPmhDefaultMapper();

        return new OaiPmhGateway(gatewayConfig, oaiPmhClient, oaiPmhMapper);
    }

    @Bean
    OaiPmhClient oaiPmhClient(RestClient.Builder builder) {
        final JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(30L));

        final RestClient restClient = builder
                .requestFactory(requestFactory)
                .build();

        return new OaiPmhClient(restClient);
    }
}
