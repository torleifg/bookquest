package com.github.torleifg.bookquest.gateway.oai_pmh;

import com.github.torleifg.bookquest.core.repository.LastModifiedRepository;
import com.github.torleifg.bookquest.core.repository.ResumptionTokenRepository;
import com.github.torleifg.bookquest.core.service.GatewayService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
class OaiPmhConfig {

    @Bean
    List<GatewayService> oaiPmhGateways(OaiPmhClient oaiPmhClient, OaiPmhProperties oaiPmhProperties, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        return oaiPmhProperties.getGateways().stream()
                .filter(OaiPmhProperties.GatewayConfig::isEnabled)
                .map(config -> createGateway(config, oaiPmhClient, resumptionTokenRepository, lastModifiedRepository))
                .map(GatewayService.class::cast)
                .toList();
    }

    private OaiPmhGateway createGateway(OaiPmhProperties.GatewayConfig gatewayConfig, OaiPmhClient oaiPmhClient, ResumptionTokenRepository resumptionTokenRepository, LastModifiedRepository lastModifiedRepository) {
        final String mapper = gatewayConfig.getMapper();

        /*
        Mapper factory
         */

        final OaiPmhMapper oaiPmhMapper = new OaiPmhDefaultMapper();

        return new OaiPmhGateway(gatewayConfig, oaiPmhClient, oaiPmhMapper, resumptionTokenRepository, lastModifiedRepository);
    }

    @Bean
    OaiPmhClient oaiPmhClient(RestClient.Builder builder) {
        final RestClient restClient = builder
                .requestFactory(new JdkClientHttpRequestFactory())
                .build();

        return new OaiPmhClient(restClient);
    }
}
